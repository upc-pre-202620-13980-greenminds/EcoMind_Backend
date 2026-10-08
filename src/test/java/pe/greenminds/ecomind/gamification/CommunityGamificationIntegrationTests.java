package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.community.application.commandservices.CommunityCommandService;
import pe.greenminds.ecomind.community.domain.model.commands.CreateLocalCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.commands.CreateTopicCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.commands.JoinCommunityCommand;
import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.RankingQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ShareAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementShareStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.infrastructure.events.GamificationOutboxDeliveryService;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.GamificationOutboxPersistenceRepository;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Actual Community, Users and Gamification suppliers; no mocked membership or publication facade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(CommunityGamificationIntegrationTests.FaultConfiguration.class)
class CommunityGamificationIntegrationTests {
    static final UserId USER = new UserId(7101L),
            OTHER = new UserId(7102L),
            OUTSIDER = new UserId(7103L);
    static final Instant AT = Instant.parse("2026-10-08T03:00:00Z");
    @Autowired CommunityCommandService communities;
    @Autowired CommunityContextFacade community;
    @Autowired AchievementCommandService achievements;
    @Autowired AchievementQueryService queries;
    @Autowired RewardCommandService rewards;
    @Autowired RankingQueryService rankings;
    @Autowired GamificationOutboxDeliveryService delivery;
    @Autowired GamificationOutboxPersistenceRepository outbox;
    @Autowired UserProfileRepository profiles;
    @Autowired PlatformTransactionManager transactions;
    @Autowired TokenService tokens;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc http;
    @Autowired ConfirmationFailure failure;
    Long communityId;

    @BeforeEach
    void prepare() {
        failure.enabled.set(false);
        for (var table :
                List.of(
                        "community_achievement_posts",
                        "community_achievement_notices",
                        "community_memberships",
                        "community_achievements",
                        "community_communities",
                        "\"gamification_outbox\"",
                        "achievement_share_requests",
                        "streak_protection_requests",
                        "achievement_awards",
                        "achievement_milestones",
                        "achievement_milestone_locks",
                        "achievements",
                        "gamification_minigame_completions",
                        "gamification_quest_experiences",
                        "reward_transactions",
                        "user_progresses",
                        "family_scores",
                        "gem_movements",
                        "processed_monetization_requests",
                        "user_profiles")) jdbc.update("DELETE FROM " + table);
        tx(
                () -> {
                    profiles.save(
                            UserProfile.create(
                                    new pe.greenminds.ecomind.users.domain.model.valueobjects
                                            .UserId(USER.value()),
                                    "Ana Flores",
                                    SocialRole.PARENT));
                    profiles.save(
                            UserProfile.create(
                                    new pe.greenminds.ecomind.users.domain.model.valueobjects
                                            .UserId(OTHER.value()),
                                    "Bruno Salazar",
                                    SocialRole.STUDENT));
                    profiles.save(
                            UserProfile.create(
                                    new pe.greenminds.ecomind.users.domain.model.valueobjects
                                            .UserId(OUTSIDER.value()),
                                    "Elena Vargas",
                                    SocialRole.STUDENT));
                });
        communityId =
                communities
                        .handle(
                                new CreateLocalCommunityCommand(
                                        "Jardines del Sol",
                                        "Cuidamos nuestro barrio",
                                        "Surco",
                                        null,
                                        USER.value()))
                        .toOptional()
                        .orElseThrow()
                        .getId();
        assertTrue(
                communities
                        .handle(new JoinCommunityCommand(communityId, OTHER.value()))
                        .isSuccess());
    }

    @Test
    void publishedNumericMembershipsSupplyLocalRankingWithoutGrantingRewards() throws Exception {
        assertEquals(communityId, community.findLocalCommunity(OTHER.value()).orElseThrow());
        assertTrue(community.findLocalCommunity(OUTSIDER.value()).isEmpty());
        assertFalse(community.isMember(communityId, OUTSIDER.value()));
        grant(USER, 19);
        grant(OTHER, 7);
        grant(OUTSIDER, 90);
        var page = rankings.participants(RankingType.LOCAL, OTHER, 0, 20);
        assertEquals(
                List.of(USER.value(), OTHER.value()),
                page.items().stream().map(p -> p.beneficiaryId()).toList());
        assertEquals(List.of(19L, 7L), page.items().stream().map(p -> p.totalEcopoints()).toList());
        assertEquals(3, count("reward_transactions"));
        http.perform(
                        get("/api/v1/gamification/rankings/LOCAL/participants")
                                .header("Authorization", bearer(OTHER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void publicationIsPersistedAndExactlyConfirmedWithNumericIds() throws Exception {
        var award = unlock();
        UUID request = UUID.randomUUID();
        http.perform(
                        post("/api/v1/gamification/achievement-shares")
                                .header("Authorization", bearer(USER))
                                .contentType("application/json")
                                .content(
                                        "{\"requestId\":\"%s\",\"awardId\":\"%s\",\"communityId\":%d}"
                                                .formatted(request, award, communityId)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
        assertEquals(0, count("community_achievement_posts"));
        deliverAll();
        var share = queries.shareStatus(request, USER).toOptional().orElseThrow();
        assertEquals(AchievementShareStatus.PUBLISHED, share.status());
        assertEquals(1, count("community_achievement_posts"));
        assertEquals(
                share.publicationId(),
                jdbc.queryForObject("SELECT id FROM community_achievement_posts", Long.class));
        assertNotNull(share.confirmedAt());
        assertNotNull(outbox.findById(shareMessage()).orElseThrow().getDeliveredAt());
        community.requestAchievementPublication(
                new CommunityContextFacade.PublishAchievement(
                        request, award, USER.value(), communityId));
        deliverAll();
        assertEquals(1, count("community_achievement_posts"));
        http.perform(
                        get("/api/v1/Community/Communities/" + communityId + "/AchievementPosts")
                                .header("Authorization", bearer(OTHER))
                                .param("authorId", USER.value().toString())
                                .param("awardId", award.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(share.publicationId()))
                .andExpect(jsonPath("$[0].requestId").value(request.toString()));
    }

    @Test
    void achievementNoticeNeverCreatesAPostAndDeduplicatesItsAward() {
        var award = unlock();
        deliverAll();
        assertEquals(1, count("community_achievement_notices"));
        assertEquals(0, count("community_achievement_posts"));
        var notice =
                new CommunityContextFacade.AchievementNotice(
                        award,
                        UUID.fromString(
                                jdbc.queryForObject(
                                        "SELECT achievement_id FROM achievement_awards",
                                        String.class)),
                        USER.value(),
                        AT);
        community.recordAchievementNotice(notice);
        assertEquals(1, count("community_achievement_notices"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        community.recordAchievementNotice(
                                new CommunityContextFacade.AchievementNotice(
                                        award, UUID.randomUUID(), USER.value(), AT)));
    }

    @Test
    void ownershipMembershipAndPublicationReadsRejectOutsiders() throws Exception {
        var award = unlock();
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        UUID.randomUUID(), award, OTHER.value(), communityId))
                        .isFailure());
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        UUID.randomUUID(), award, USER.value(), communityId + 500))
                        .isFailure());
        http.perform(
                        get("/api/v1/Community/Communities/" + communityId + "/AchievementPosts")
                                .header("Authorization", bearer(OUTSIDER)))
                .andExpect(status().isForbidden());
        http.perform(get("/api/v1/Community/Communities/" + communityId + "/AchievementPosts"))
                .andExpect(status().isUnauthorized());
        assertEquals(0, count("achievement_share_requests"));
    }

    @Test
    void membershipRevokedBeforeDeliveryLeavesRequestPendingWithoutCreatingPost() {
        UUID request = share();
        jdbc.update(
                "DELETE FROM community_memberships WHERE community_id=? AND user_id=?",
                communityId,
                USER.value());
        assertThrows(SecurityException.class, () -> delivery.deliver(shareMessage()));
        assertEquals(
                AchievementShareStatus.PENDING,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
        assertEquals(0, count("community_achievement_posts"));
        assertNull(outbox.findById(shareMessage()).orElseThrow().getDeliveredAt());
    }

    @Test
    void confirmationFailureRollsBackPostAndAllowsOneSuccessfulRetry() {
        UUID request = share();
        failure.enabled.set(true);
        assertThrows(IllegalStateException.class, () -> delivery.deliver(shareMessage()));
        assertEquals(0, count("community_achievement_posts"));
        assertEquals(
                AchievementShareStatus.PENDING,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
        assertNull(outbox.findById(shareMessage()).orElseThrow().getDeliveredAt());
        failure.enabled.set(false);
        delivery.deliver(shareMessage());
        assertEquals(1, count("community_achievement_posts"));
        assertEquals(
                AchievementShareStatus.PUBLISHED,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
    }

    @Test
    void concurrentOutboxDeliveryCreatesOnlyOnePost() throws Exception {
        UUID request = share();
        String message = shareMessage();
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first =
                    pool.submit(
                            () -> {
                                gate.await();
                                delivery.deliver(message);
                                return true;
                            });
            var second =
                    pool.submit(
                            () -> {
                                gate.await();
                                delivery.deliver(message);
                                return true;
                            });
            gate.countDown();
            assertTrue(first.get(20, TimeUnit.SECONDS));
            assertTrue(second.get(20, TimeUnit.SECONDS));
        }
        assertEquals(1, count("community_achievement_posts"));
        assertEquals(
                AchievementShareStatus.PUBLISHED,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
    }

    @Test
    void publicationRetryRejectsChangedSelectionAndCannotReplacePersistedPost() {
        UUID request = share();
        delivery.deliver(shareMessage());
        var published = queries.shareStatus(request, USER).toOptional().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        community.requestAchievementPublication(
                                new CommunityContextFacade.PublishAchievement(
                                        request, UUID.randomUUID(), USER.value(), communityId)));
        assertEquals(published, queries.shareStatus(request, USER).toOptional().orElseThrow());
        assertEquals(1, count("community_achievement_posts"));
    }

    @Test
    void communityMutationsRequireTheJwtOwnerAndParentRole() throws Exception {
        http.perform(
                        post("/api/v1/Community/Communities/Topics")
                                .header("Authorization", bearer(OTHER))
                                .contentType("application/json")
                                .content(
                                        "{\"name\":\"Huertos\",\"topic\":\"Gardening\",\"member_limit\":10,\"user_id\":7101}"))
                .andExpect(status().isForbidden());
        http.perform(
                        post("/api/v1/Community/Communities/" + communityId + "/Memberships")
                                .header("Authorization", bearer(OUTSIDER))
                                .param("user_id", "7101"))
                .andExpect(status().isForbidden());
        assertTrue(
                communities
                        .handle(
                                new CreateTopicCommunityCommand(
                                        "Huertos",
                                        "Cultivamos juntos",
                                        "Gardening",
                                        10,
                                        null,
                                        OTHER.value()))
                        .isFailure());
        assertTrue(
                communities
                        .handle(
                                new CreateTopicCommunityCommand(
                                        "Huertos",
                                        "Cultivamos juntos",
                                        "Gardening",
                                        10,
                                        null,
                                        USER.value()))
                        .isSuccess());
    }

    @Test
    void shareRejectsNonPositiveCommunityId() throws Exception {
        UUID award = unlock();
        http.perform(
                        post("/api/v1/gamification/achievement-shares")
                                .header("Authorization", bearer(USER))
                                .contentType("application/json")
                                .content(
                                        "{\"requestId\":\"%s\",\"awardId\":\"%s\",\"communityId\":0}"
                                                .formatted(UUID.randomUUID(), award)))
                .andExpect(status().isBadRequest());
        assertEquals(0, count("achievement_share_requests"));
    }

    private UUID unlock() {
        achievements.register(
                new Achievement(
                        UUID.randomUUID(),
                        "GREEN_STEPS",
                        "Green steps",
                        "A little progress each day",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        10,
                        true));
        grant(USER, 12);
        return queries.forUser(USER, 0, 20).getFirst().id();
    }

    private void grant(UserId user, long points) {
        rewards.handle(
                new GrantQuestRewardCommand(
                        UUID.randomUUID(),
                        user,
                        AT,
                        LocalDate.of(2026, 10, 7),
                        false,
                        new Reward(points, 0, 0)));
    }

    private UUID share() {
        var command =
                new ShareAchievementCommand(UUID.randomUUID(), unlock(), USER.value(), communityId);
        assertTrue(achievements.handle(command).isSuccess());
        return command.requestId();
    }

    private String shareMessage() {
        return outbox.findAll().stream()
                .filter(m -> m.getMessageType().equals("SHARE_ACHIEVEMENT"))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private void deliverAll() {
        for (var message : outbox.findAll()) delivery.deliver(message.getId());
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    private void tx(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(s -> action.run());
    }

    private String bearer(UserId user) {
        return "Bearer "
                + tokens.issueAccessToken(
                                new AuthenticatedUser(
                                        new AccountId(user.value()),
                                        new EmailAddress("ana.flores@example.net")))
                        .value();
    }

    @TestConfiguration
    static class FaultConfiguration {
        @Bean
        ConfirmationFailure confirmationFailure() {
            return new ConfirmationFailure();
        }
    }

    static class ConfirmationFailure {
        final AtomicBoolean enabled = new AtomicBoolean();

        @EventListener
        void confirm(PublicationCreatedIntegrationEvent event) {
            if (enabled.get()) throw new IllegalStateException("Temporary confirmation failure");
        }
    }
}
