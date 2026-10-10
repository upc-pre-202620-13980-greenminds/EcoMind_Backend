package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.FamilyScorePersistenceRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.RewardTransactionPersistenceRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.UserProgressPersistenceRepository;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.quests.interfaces.acl.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories.FamilyPersistenceRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(
        properties =
                "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:familygamification;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_AND_AFTER_CLASS)
class FamilyGamificationTests {
    @Autowired FamilyRewardCommandService rewards;
    @Autowired RewardCommandService individualRewards;
    @Autowired GamificationQueryService individualQueries;
    @Autowired FamilyScorePersistenceRepository scores;
    @Autowired RewardTransactionPersistenceRepository rewardRows;
    @Autowired UserProgressPersistenceRepository progress;
    @Autowired FamilyRepository families;
    @Autowired FamilyPersistenceRepository familyRows;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MockMvc http;
    @Autowired TokenService tokens;
    @Autowired ApplicationEventPublisher events;
    @Autowired AchievementCommandService achievements;
    @Autowired JdbcTemplate jdbc;
    Family family;
    FamilyId familyId;
    static final long MEMBER = 6001L;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM achievement_awards");
        jdbc.update("DELETE FROM achievements");
        jdbc.update("DELETE FROM achievement_milestones");
        rewardRows.deleteAll();
        scores.deleteAll();
        progress.deleteAll();
        familyRows.deleteAll();
        family =
                new TransactionTemplate(transactions)
                        .execute(
                                status ->
                                        families.save(
                                                Family.create(
                                                        new pe.greenminds.ecomind.users.domain.model
                                                                .valueobjects.UserId(MEMBER),
                                                        "Green family",
                                                        "Save water")));
        familyId = new FamilyId(family.getId().value());
    }

    @Test
    void currentMemberReadsZeroScoreBeforeAnyReward() throws Exception {
        http.perform(get(path("score")).header("Authorization", bearer(MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEcopoints").value(0));
        http.perform(get(path("rewards")).header("Authorization", bearer(MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        assertEquals(0, scores.count());
    }

    @Test
    void accessRequiresAuthenticationAndCurrentMembership() throws Exception {
        for (String endpoint : new String[] {"score", "rewards"}) {
            http.perform(get(path(endpoint))).andExpect(status().isUnauthorized());
            http.perform(get(path(endpoint)).header("Authorization", bearer(6002L)))
                    .andExpect(status().isForbidden());
        }
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status -> {
                            family.removeMember(family.getMembers().getFirst().getId());
                            families.save(family);
                        });
        http.perform(get(path("score")).header("Authorization", bearer(MEMBER)))
                .andExpect(status().isForbidden());
        http.perform(
                        get("/api/v1/gamification/families/-1/score")
                                .header("Authorization", bearer(MEMBER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void familyRewardIsIdempotentAndDoesNotModifyIndividualProgress() throws Exception {
        UUID execution = UUID.randomUUID();
        var command = command(execution, 50);
        var first = rewards.handle(command).toOptional().orElseThrow();
        assertEquals(first.id(), rewards.handle(command).toOptional().orElseThrow().id());
        // Same numeric identity and execution, but a different beneficiary type/source.
        var userId = new UserId(familyId.value());
        individualRewards.handle(
                new GrantQuestRewardCommand(
                        execution,
                        userId,
                        Instant.now(),
                        LocalDate.of(2026, 10, 7),
                        true,
                        new Reward(7, 0)));
        assertEquals(7, individualQueries.getUserProgress(userId).getTotalEcopoints());
        assertEquals(1, individualQueries.getRecentRewards(userId).size());
        http.perform(get(path("score")).header("Authorization", bearer(MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEcopoints").value(50));
        http.perform(get(path("rewards")).header("Authorization", bearer(MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sourceType").value("FAMILY_PLAN"));
        var stored = rewardRows.findById(first.id().toString()).orElseThrow();
        assertEquals(0, stored.getGems());
    }

    @Test
    void simultaneousPlanDeliveriesGrantOnce() throws Exception {
        var command = command(UUID.randomUUID(), 30);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () -> {
                                start.await();
                                return rewards.handle(command).toOptional().orElseThrow();
                            });
            var second =
                    executor.submit(
                            () -> {
                                start.await();
                                return rewards.handle(command).toOptional().orElseThrow();
                            });
            start.countDown();
            assertEquals(
                    first.get(10, TimeUnit.SECONDS).id(), second.get(10, TimeUnit.SECONDS).id());
        }
        assertEquals(1, rewardRows.count());
        assertEquals(30, scores.findById(familyId.value()).orElseThrow().getTotalEcopoints());
    }

    @Test
    void publishedFamilyPlanRetryRecognizesOnceWithoutRepeatingIndividualQuestRewards() {
        achievements.register(
                new Achievement(
                        UUID.randomUUID(),
                        "FAMILY_TEAM",
                        "Family team",
                        "We finished a plan together",
                        AchievementScope.FAMILY,
                        AchievementMetric.COMPLETED_FAMILY_PLANS,
                        1,
                        true));
        individualRewards.handle(
                new GrantQuestRewardCommand(
                        UUID.randomUUID(),
                        new UserId(MEMBER),
                        Instant.now(),
                        LocalDate.of(2026, 10, 7),
                        false,
                        new Reward(12, 0)));
        for (int i = 0; i < 2; i++) {
            var event =
                    new FamilyPlanCompletedIntegrationEvent(
                            UUID.randomUUID(),
                            77L,
                            familyId.value(),
                            MEMBER,
                            List.of(MEMBER),
                            Instant.parse("2026-10-08T03:00:00Z").atOffset(ZoneOffset.UTC));
            new TransactionTemplate(transactions)
                    .executeWithoutResult(s -> events.publishEvent(event));
        }
        assertEquals(12, individualQueries.getUserProgress(new UserId(MEMBER)).getTotalEcopoints());
        assertEquals(0, scores.findById(familyId.value()).orElseThrow().getTotalEcopoints());
        assertEquals(2, rewardRows.count());
        assertEquals(
                1,
                jdbc.queryForObject("SELECT COUNT(*) FROM achievement_milestones", Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM achievement_awards WHERE scope='FAMILY'",
                        Integer.class));
    }

    @Test
    void overflowRollsBackRewardAndPreservesExistingScore() {
        rewards.handle(command(UUID.randomUUID(), Long.MAX_VALUE));
        assertThrows(
                ArithmeticException.class, () -> rewards.handle(command(UUID.randomUUID(), 1)));
        assertEquals(1, rewardRows.count());
        assertEquals(
                Long.MAX_VALUE,
                scores.findById(familyId.value()).orElseThrow().getTotalEcopoints());
    }

    @Test
    void missingFamilyAndNegativeRewardCannotCreateProgress() {
        assertTrue(
                rewards.handle(
                                new GrantFamilyPlanRewardCommand(
                                        UUID.randomUUID(),
                                        new FamilyId(Long.MAX_VALUE),
                                        20,
                                        Instant.now()))
                        .isFailure());
        assertThrows(IllegalArgumentException.class, () -> command(UUID.randomUUID(), -1));
        assertEquals(0, scores.count());
        assertEquals(0, rewardRows.count());
    }

    @Test
    void separatePlanExecutionsAccumulateOnlyTheirConfiguredAdditionalReward() {
        rewards.handle(command(UUID.randomUUID(), 20));
        rewards.handle(command(UUID.randomUUID(), 30));
        rewards.handle(command(UUID.randomUUID(), 0));
        assertEquals(50, scores.findById(familyId.value()).orElseThrow().getTotalEcopoints());
        assertEquals(0, progress.count());
    }

    private GrantFamilyPlanRewardCommand command(UUID execution, long points) {
        return new GrantFamilyPlanRewardCommand(execution, familyId, points, Instant.now());
    }

    private String path(String suffix) {
        return "/api/v1/gamification/families/" + familyId.value() + "/" + suffix;
    }

    private String bearer(long id) {
        return "Bearer "
                + tokens.issueAccessToken(
                                new AuthenticatedUser(
                                        new AccountId(id), new EmailAddress(id + "@example.com")))
                        .value();
    }
}
