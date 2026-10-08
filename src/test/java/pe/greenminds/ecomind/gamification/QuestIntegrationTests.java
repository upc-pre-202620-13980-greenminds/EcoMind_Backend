package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.MonetizationServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.QuestServiceClient;
import pe.greenminds.ecomind.gamification.interfaces.acl.GamificationContextFacade;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.acl.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.resources.QuestRewardResource;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Validates the proposed reward-complete ACL contract, not the real Quests completion workflow. */
@SpringBootTest(
        properties =
                "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:questintegration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class QuestIntegrationTests {
    @Autowired MonetizationServiceClient monetization;
    @Autowired CommunityServiceClient community;
    @Autowired QuestServiceClient quests;
    @Autowired QuestEventPublisher publisher;
    @Autowired GamificationContextFacade progress;
    @Autowired PlatformTransactionManager transactions;
    @Autowired JdbcTemplate jdbc;
    @Autowired FamilyRepository families;
    static final long USER = 9101;
    static final Instant NOW = Instant.parse("2026-10-07T17:00:00Z");
    static final LocalDate DAY = LocalDate.of(2026, 10, 7);

    @BeforeEach
    void clean() {
        for (String table :
                new String[] {
                    "achievement_awards",
                    "achievements",
                    "reward_transactions",
                    "user_progresses",
                    "family_scores",
                    "family_members",
                    "families"
                }) jdbc.update("DELETE FROM " + table);
    }

    @Test
    void newMessageIdForSameExecutionDoesNotGrantAgain() {
        UUID execution = UUID.randomUUID();
        new TransactionTemplate(transactions)
                .executeWithoutResult(status -> publisher.publish(quest(execution, 0)));
        new TransactionTemplate(transactions)
                .executeWithoutResult(status -> publisher.publish(quest(execution, 0)));
        var snapshot = progress.getUserProgress(USER);
        assertEquals(15, snapshot.ecopoints());
        assertEquals(8, snapshot.experience());
        assertEquals(1, snapshot.currentStreak());
        assertEquals(DAY, snapshot.lastActivityDate());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void publicationRequiresAnExistingCompletionTransaction() {
        assertThrows(
                IllegalTransactionStateException.class,
                () -> publisher.publish(quest(UUID.randomUUID(), 0)));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void failingConsumerRollsBackEarlierGrantsInTheSameCompletionTransaction() {
        assertThrows(
                IllegalStateException.class,
                () ->
                        new TransactionTemplate(transactions)
                                .executeWithoutResult(
                                        status -> {
                                            publisher.publish(quest(UUID.randomUUID(), 0));
                                            publisher.publish(
                                                    new FamilyPlanCompletedIntegrationEvent(
                                                            UUID.randomUUID(),
                                                            UUID.randomUUID(),
                                                            UUID.randomUUID(),
                                                            Long.MAX_VALUE,
                                                            List.of(USER),
                                                            25,
                                                            NOW));
                                        }));
        assertEquals(0, progress.getUserProgress(USER).ecopoints());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void familyCompletionGrantsOnlyTheConfiguredBonusAndRejectsUnknownFamily() {
        var family =
                new TransactionTemplate(transactions)
                        .execute(
                                status ->
                                        families.save(
                                                Family.create(
                                                        new UserId(USER), "Family", "Save water")));
        UUID execution = UUID.randomUUID();
        UUID plan = UUID.randomUUID();
        for (int delivery = 0; delivery < 2; delivery++) {
            new TransactionTemplate(transactions)
                    .executeWithoutResult(
                            status ->
                                    publisher.publish(
                                            new FamilyPlanCompletedIntegrationEvent(
                                                    UUID.randomUUID(),
                                                    execution,
                                                    plan,
                                                    family.getId().value(),
                                                    List.of(USER),
                                                    25,
                                                    NOW)));
        }
        assertEquals(
                25,
                jdbc.queryForObject(
                        "SELECT total_ecopoints FROM family_scores WHERE family_id = ?",
                        Long.class,
                        family.getId().value()));
        assertEquals(0, progress.getUserProgress(USER).ecopoints());
        assertThrows(
                IllegalStateException.class,
                () ->
                        new TransactionTemplate(transactions)
                                .executeWithoutResult(
                                        status ->
                                                publisher.publish(
                                                        new FamilyPlanCompletedIntegrationEvent(
                                                                UUID.randomUUID(),
                                                                UUID.randomUUID(),
                                                                plan,
                                                                Long.MAX_VALUE,
                                                                List.of(USER),
                                                                25,
                                                                NOW))));
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void suppliersReportRealAbsenceAndUnimplementedContextsStillFailExplicitly() {
        assertTrue(monetization.getActiveMultiplier(USER, NOW).isEmpty());
        assertFalse(community.isMember(73L, USER));
        assertThrows(
                IllegalStateException.class,
                () -> quests.findValidatedMinigameAttempt(UUID.randomUUID()));
    }

    private QuestCompletedIntegrationEvent quest(UUID execution, int gems) {
        return new QuestCompletedIntegrationEvent(
                UUID.randomUUID(),
                execution,
                UUID.randomUUID(),
                USER,
                "WATER",
                NOW,
                DAY,
                true,
                new QuestRewardResource(15, 8, gems));
    }
}
