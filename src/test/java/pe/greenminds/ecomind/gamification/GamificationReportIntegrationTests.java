package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static pe.greenminds.ecomind.monetization.application.internal.eventhandlers.MonetizationCatalogSeed.LEAF_AVATAR;
import static pe.greenminds.ecomind.monetization.application.internal.eventhandlers.MonetizationCatalogSeed.STREAK_SHIELD;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.Category.WATER;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus.COMPLETED;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.ACTIVITIES;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.DAILY_QUEST;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventCompletedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalCompletedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.StreakProtectionCommandService;
import pe.greenminds.ecomind.gamification.application.internal.services.DailyStreakClosureService;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.RankingQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.commands.ConfirmAchievementPublicationCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCollaborativeQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantMinigameRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.RequestStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ResolveStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ShareAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementShareStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.StreakProtectionStatus;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementShareRequestRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.StreakProtectionRequestRepository;
import pe.greenminds.ecomind.gamification.infrastructure.events.GamificationOutboxDeliveryService;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.GamificationOutboxPersistenceRepository;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;
import pe.greenminds.ecomind.quests.interfaces.acl.events.MinigameCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest(
        properties =
                "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:gamificationreport;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GamificationReportIntegrationTests {
    @Autowired RewardCommandService rewards;
    @Autowired AchievementCommandService achievements;
    @Autowired StreakProtectionCommandService protection;
    @Autowired GamificationQueryService progress;
    @Autowired AchievementQueryService queries;
    @Autowired RankingQueryService rankings;
    @Autowired AchievementShareRequestRepository shares;
    @Autowired StreakProtectionRequestRepository protections;
    @Autowired GamificationOutboxPersistenceRepository outbox;
    @Autowired GamificationOutboxDeliveryService delivery;
    @Autowired DailyStreakClosureService closure;
    @Autowired ApplicationEventPublisher events;
    @Autowired PlatformTransactionManager transactions;
    @Autowired UserProfileRepository profiles;
    @Autowired TokenService tokens;
    @Autowired MockMvc http;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CommunityContextFacade community;
    static final UserId USER = new UserId(5101L), OTHER = new UserId(5102L);
    static final Instant AT = Instant.parse("2026-10-07T17:00:00Z");
    static final LocalDate DAY = LocalDate.of(2026, 10, 7);
    final Long communityId = 73L;

    @BeforeEach
    void prepare() {
        for (var table :
                List.of(
                        "\"gamification_outbox\"",
                        "achievement_share_requests",
                        "streak_protection_requests",
                        "achievement_awards",
                        "achievement_milestones",
                        "achievement_milestone_locks",
                        "achievements",
                        "gamification_minigame_completions",
                        "minigame_attempts",
                        "reward_transactions",
                        "user_progresses",
                        "family_scores",
                        "gem_movements",
                        "processed_monetization_requests",
                        "protector_inventories",
                        "user_multipliers",
                        "user_cosmetics",
                        "user_profiles")) jdbc.update("DELETE FROM " + table);
        tx(
                () -> {
                    profiles.save(
                            UserProfile.create(
                                    new pe.greenminds.ecomind.users.domain.model.valueobjects
                                            .UserId(USER.value()),
                                    "Lucia Torres",
                                    SocialRole.STUDENT));
                    profiles.save(
                            UserProfile.create(
                                    new pe.greenminds.ecomind.users.domain.model.valueobjects
                                            .UserId(OTHER.value()),
                                    "Daniel Rojas",
                                    SocialRole.STUDENT));
                });
        when(community.isMember(communityId, USER.value())).thenReturn(true);
        when(community.mayPublishAchievement(communityId, USER.value())).thenReturn(true);
        when(community.findLocalCommunity(USER.value())).thenReturn(Optional.of(communityId));
        when(community.findMembers(communityId))
                .thenReturn(
                        List.of(new CommunityContextFacade.Member(USER.value(), "Lucia Torres")));
    }

    @Test
    void repeatedMinigameAttemptsDecreaseToZeroAndResetAfterThreeHours() {
        var game = UUID.randomUUID();
        var points = new ArrayList<Long>();
        for (int i = 0; i < 6; i++)
            points.add(
                    rewards.handle(
                                    new GrantMinigameRewardCommand(
                                            UUID.randomUUID(),
                                            game,
                                            USER,
                                            AT.plusSeconds(i),
                                            new Reward(100, 10)))
                            .grantedReward()
                            .ecopoints());
        assertEquals(List.of(100L, 80L, 50L, 20L, 0L, 0L), points);
        var renewed =
                rewards.handle(
                        new GrantMinigameRewardCommand(
                                UUID.randomUUID(),
                                game,
                                USER,
                                AT.plusSeconds(10806),
                                new Reward(100, 10)));
        assertEquals(100, renewed.grantedReward().ecopoints());
        assertEquals(0, progress.getUserProgress(USER).getCurrentStreak());
    }

    @Test
    void minigameRetryDoesNotConsumeAnotherRepetitionAndUsersHaveIndependentWindows() {
        var game = UUID.randomUUID();
        var execution = UUID.randomUUID();
        var c = new GrantMinigameRewardCommand(execution, game, USER, AT, new Reward(10, 0));
        assertEquals(rewards.handle(c).id(), rewards.handle(c).id());
        assertEquals(
                10,
                rewards.handle(
                                new GrantMinigameRewardCommand(
                                        UUID.randomUUID(), game, OTHER, AT, new Reward(10, 0)))
                        .grantedReward()
                        .ecopoints());
        assertEquals(
                8,
                rewards.handle(
                                new GrantMinigameRewardCommand(
                                        UUID.randomUUID(), game, USER, AT, new Reward(10, 0)))
                        .grantedReward()
                        .ecopoints());
    }

    @Test
    void multiplierBoostsTheEcopointsScoreWithinItsValidityAndLeavesGemsUnchanged() {
        jdbc.update(
                "INSERT INTO user_multipliers"
                        + " (id,user_id,multiplier_id,factor,starts_at,expires_at) VALUES"
                        + " (?,?,?,?,?,?)",
                UUID.randomUUID().toString(),
                USER.value(),
                UUID.randomUUID().toString(),
                new BigDecimal("2.5"),
                java.sql.Timestamp.from(AT),
                java.sql.Timestamp.from(AT.plusSeconds(60)));
        var active = rewards.handle(quest(USER, AT, false, new Reward(10, 3)));
        assertEquals(new Reward(25, 3), active.grantedReward());
        assertNotNull(active.multiplierId());
        assertEquals(new BigDecimal("2.5"), active.appliedFactor());
        assertEquals(
                active.multiplierId(), progress.getRecentRewards(USER).getFirst().multiplierId());
        var expired = rewards.handle(quest(USER, AT.plusSeconds(60), false, new Reward(10, 3)));
        assertEquals(new Reward(10, 3), expired.grantedReward());
    }

    @Test
    void historicalXpColumnsDoNotCreateAnotherScoreOrChangeRewardAmounts() throws Exception {
        var grant = rewards.handle(quest(USER, AT, false, new Reward(19, 4)));
        jdbc.update("UPDATE user_progresses SET total_experience=900 WHERE user_id=?", USER.value());
        jdbc.update(
                "UPDATE reward_transactions SET base_experience=500,experience=750 WHERE id=?",
                grant.id().toString());
        assertEquals(19, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(new Reward(19, 4), progress.getRecentRewards(USER).getFirst().grantedReward());
        http.perform(get("/api/v1/gamification/me/progress").header("Authorization", bearer(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEcopoints").value(19))
                .andExpect(jsonPath("$.totalExperience").doesNotExist());
        http.perform(get("/api/v1/gamification/me/rewards").header("Authorization", bearer(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ecopoints").value(19))
                .andExpect(jsonPath("$[0].gems").value(4))
                .andExpect(jsonPath("$[0].experience").doesNotExist());
    }

    @Test
    void gemDeliveryPersistsOnceAndARepeatedDeliveryDoesNotCreditAgain() {
        var grant = rewards.handle(quest(USER, AT, false, new Reward(10, 3)));
        var message = outbox.findAll().getFirst();
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT gem_balance FROM user_profiles WHERE user_id=?",
                        Integer.class,
                        USER.value()));
        delivery.deliver(message.getId());
        delivery.deliver(message.getId());
        assertEquals(
                3,
                jdbc.queryForObject(
                        "SELECT gem_balance FROM user_profiles WHERE user_id=?",
                        Integer.class,
                        USER.value()));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gem_movements WHERE reference_id=?",
                        Integer.class,
                        grant.id().toString()));
    }

    @Test
    void collaborativeSessionAwardsEachValidatedParticipantOnceWithoutFamilyAttribution() {
        var c =
                new GrantCollaborativeQuestRewardCommand(
                        UUID.randomUUID(), List.of(OTHER, USER), AT, new Reward(15, 2));
        var first = rewards.handle(c);
        var duplicate = rewards.handle(c);
        assertEquals(first, duplicate);
        assertEquals(15, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(15, progress.getUserProgress(OTHER).getTotalEcopoints());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM family_scores", Integer.class));
    }

    @Test
    void communityCompletionRecognizesCollectiveAndEligibleIndividualAwardsWithoutInventedPoints() {
        achievements.register(
                achievement(
                        "COMMUNITY_TEAM",
                        AchievementScope.COMMUNITY,
                        AchievementMetric.COMPLETED_COMMUNITY_GOALS,
                        1));
        achievements.register(
                achievement(
                        "COMMUNITY_MEMBER",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.COMPLETED_COMMUNITY_GOALS,
                        1));
        var execution = UUID.randomUUID();
        var goal = UUID.randomUUID();
        for (int i = 0; i < 2; i++)
            tx(
                    () ->
                            events.publishEvent(
                                    new CommunityGoalCompletedIntegrationEvent(
                                            UUID.randomUUID(),
                                            execution,
                                            goal,
                                            communityId,
                                            List.of(USER.value()),
                                            null,
                                            AT)));
        assertEquals(
                1,
                queries.forCommunity(communityId, USER, 0, 20).toOptional().orElseThrow().size());
        assertEquals(1, queries.forUser(USER, 0, 20).size());
        assertEquals(0, queries.forUser(OTHER, 0, 20).size());
        assertEquals(0, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void communityEventAwardsOnlyItsConfiguredRewardAndNeverChangesDailyStreak() {
        var execution = UUID.randomUUID();
        for (int i = 0; i < 2; i++)
            tx(
                    () ->
                            events.publishEvent(
                                    new CommunityEventCompletedIntegrationEvent(
                                            UUID.randomUUID(),
                                            execution,
                                            communityId,
                                            List.of(USER.value()),
                                            new CommunityGoalCompletedIntegrationEvent
                                                    .ConfiguredReward(9, 1),
                                            AT)));
        assertEquals(9, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(0, progress.getUserProgress(USER).getCurrentStreak());
    }

    @Test
    void shareIsPendingUntilCorrelatedPublicationAndRepeatedRequestKeepsItsIdentity() {
        var award = unlock();
        var c =
                new ShareAchievementCommand(
                        UUID.randomUUID(), award.id(), USER.value(), communityId);
        var first = achievements.handle(c).toOptional().orElseThrow();
        assertEquals(AchievementShareStatus.PENDING, first.status());
        assertNull(first.publicationId());
        assertEquals(first, achievements.handle(c).toOptional().orElseThrow());
        Long publication = 73L;
        var confirmation =
                new ConfirmAchievementPublicationCommand(
                        c.requestId(), award.id(), USER.value(), communityId, publication);
        achievements.handle(confirmation);
        achievements.handle(confirmation);
        assertEquals(
                publication,
                queries.shareStatus(c.requestId(), USER)
                        .toOptional()
                        .orElseThrow()
                        .publicationId());
        assertTrue(queries.shareStatus(c.requestId(), OTHER).isFailure());
        assertThrows(
                IllegalStateException.class,
                () ->
                        achievements.handle(
                                new ConfirmAchievementPublicationCommand(
                                        c.requestId(),
                                        award.id(),
                                        USER.value(),
                                        communityId,
                                        94L)));
    }

    @Test
    void shareRejectsForeignAwardsMissingMembershipPermissionAndReusedSelection() {
        var award = unlock();
        var request = UUID.randomUUID();
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        request, award.id(), OTHER.value(), communityId))
                        .isFailure());
        when(community.isMember(communityId, USER.value())).thenReturn(false);
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        request, award.id(), USER.value(), communityId))
                        .isFailure());
        when(community.isMember(communityId, USER.value())).thenReturn(true);
        when(community.mayPublishAchievement(communityId, USER.value())).thenReturn(false);
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        request, award.id(), USER.value(), communityId))
                        .isFailure());
        when(community.mayPublishAchievement(communityId, USER.value())).thenReturn(true);
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        request, award.id(), USER.value(), communityId))
                        .isSuccess());
        assertTrue(
                achievements
                        .handle(
                                new ShareAchievementCommand(
                                        request, UUID.randomUUID(), USER.value(), communityId))
                        .isFailure());
    }

    @Test
    void mismatchedConfirmationDoesNotPublishAndUnrelatedPublicationIsIgnored() {
        var award = unlock();
        var request = UUID.randomUUID();
        achievements.handle(
                new ShareAchievementCommand(request, award.id(), USER.value(), communityId));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        achievements.handle(
                                new ConfirmAchievementPublicationCommand(
                                        request, award.id(), OTHER.value(), communityId, 94L)));
        achievements.handle(
                new ConfirmAchievementPublicationCommand(
                        UUID.randomUUID(), UUID.randomUUID(), OTHER.value(), communityId, 94L));
        assertEquals(
                AchievementShareStatus.PENDING,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
    }

    @Test
    void technicalPublicationFailureRemainsRetryableAndDoesNotConfirmPost() {
        var award = unlock();
        var request = UUID.randomUUID();
        achievements.handle(
                new ShareAchievementCommand(request, award.id(), USER.value(), communityId));
        var message =
                outbox.findAll().stream()
                        .filter(m -> m.getMessageType().equals("SHARE_ACHIEVEMENT"))
                        .findFirst()
                        .orElseThrow();
        doThrow(new IllegalStateException("Connection unavailable"))
                .when(community)
                .requestAchievementPublication(any());
        assertThrows(IllegalStateException.class, () -> delivery.deliver(message.getId()));
        delivery.retryLater(message.getId());
        assertNull(outbox.findById(message.getId()).orElseThrow().getDeliveredAt());
        assertEquals(
                AchievementShareStatus.PENDING,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
    }

    @Test
    void successfulPublicationDeliveryProcessesItsConfirmationWithoutChangingOwnership() {
        var award = unlock();
        var request = UUID.randomUUID();
        achievements.handle(
                new ShareAchievementCommand(request, award.id(), USER.value(), communityId));
        doAnswer(
                        call -> {
                            var c =
                                    call.getArgument(
                                            0, CommunityContextFacade.PublishAchievement.class);
                            events.publishEvent(
                                    new PublicationCreatedIntegrationEvent(
                                            UUID.randomUUID(),
                                            c.requestId(),
                                            c.awardId(),
                                            c.requestedBy(),
                                            c.communityId(),
                                            95L,
                                            AT));
                            return null;
                        })
                .when(community)
                .requestAchievementPublication(any());
        delivery.deliver(
                outbox.findAll().stream()
                        .filter(m -> m.getMessageType().equals("SHARE_ACHIEVEMENT"))
                        .findFirst()
                        .orElseThrow()
                        .getId());
        assertEquals(
                AchievementShareStatus.PUBLISHED,
                queries.shareStatus(request, USER).toOptional().orElseThrow().status());
        assertEquals(AchievementScope.INDIVIDUAL, queries.forUser(USER, 0, 20).getFirst().scope());
    }

    @Test
    void protectedDayKeepsContinuityWithoutCreatingActivityAndNextDailyQuestIncrementsOnce() {
        rewards.handle(quest(USER, AT, true, new Reward(10, 0)));
        var request =
                protection.handle(
                        new RequestStreakProtectionCommand(
                                USER, DAY.plusDays(1), AT.plusSeconds(86400)));
        var result =
                new ResolveStreakProtectionCommand(
                        request.id(), USER, DAY.plusDays(1), StreakProtectionStatus.PROTECTED);
        protection.handle(result);
        protection.handle(result);
        assertEquals(DAY, progress.getUserProgress(USER).getLastActivityDate());
        assertEquals(1, progress.getUserProgress(USER).getCurrentStreak());
        rewards.handle(quest(USER, AT.plusSeconds(172800), true, new Reward(5, 0)));
        rewards.handle(quest(USER, AT.plusSeconds(172810), true, new Reward(5, 0)));
        assertEquals(2, progress.getUserProgress(USER).getCurrentStreak());
        assertEquals(20, progress.getUserProgress(USER).getTotalEcopoints());
    }

    @Test
    void unavailableInventoryResetsStreakButTechnicalFailureStaysPending() {
        rewards.handle(quest(USER, AT, true, new Reward(10, 0)));
        var request =
                protection.handle(
                        new RequestStreakProtectionCommand(
                                USER, DAY.plusDays(1), AT.plusSeconds(86400)));
        assertEquals(
                request,
                protection.handle(
                        new RequestStreakProtectionCommand(
                                USER, DAY.plusDays(1), AT.plusSeconds(86400))));
        assertThrows(
                IllegalStateException.class,
                () -> rewards.handle(quest(USER, AT.plusSeconds(172800), true, new Reward(5, 0))));
        assertEquals(1, progress.getUserProgress(USER).getCurrentStreak());
        protection.handle(
                new ResolveStreakProtectionCommand(
                        request.id(), USER, DAY.plusDays(1), StreakProtectionStatus.UNAVAILABLE));
        assertEquals(0, progress.getUserProgress(USER).getCurrentStreak());
        assertEquals(1, progress.getUserProgress(USER).getLongestStreak());
        assertThrows(
                IllegalStateException.class,
                () ->
                        protection.handle(
                                new ResolveStreakProtectionCommand(
                                        request.id(),
                                        USER,
                                        DAY.plusDays(1),
                                        StreakProtectionStatus.PROTECTED)));
    }

    @Test
    void protectorResultCorrelationRejectsWrongUserOrDate() {
        rewards.handle(quest(USER, AT, true, new Reward(10, 0)));
        var request =
                protection.handle(new RequestStreakProtectionCommand(USER, DAY.plusDays(1), AT));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        protection.handle(
                                new ResolveStreakProtectionCommand(
                                        request.id(),
                                        OTHER,
                                        DAY.plusDays(1),
                                        StreakProtectionStatus.PROTECTED)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        protection.handle(
                                new ResolveStreakProtectionCommand(
                                        request.id(),
                                        USER,
                                        DAY.plusDays(2),
                                        StreakProtectionStatus.PROTECTED)));
        assertEquals(
                StreakProtectionStatus.PENDING,
                protections.find(USER, DAY.plusDays(1)).orElseThrow().status());
    }

    @Test
    void dailyClosureIsIdempotentAndMonetizationConfirmsNoInventory() {
        rewards.handle(quest(USER, AT, true, new Reward(10, 0)));
        closure.closeThrough(DAY.plusDays(1), AT.plusSeconds(172800));
        closure.closeThrough(DAY.plusDays(1), AT.plusSeconds(172800));
        var message = outbox.findAll().getFirst();
        delivery.deliver(message.getId());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM streak_protection_requests", Integer.class));
        assertEquals(
                StreakProtectionStatus.UNAVAILABLE,
                protections.find(USER, DAY.plusDays(1)).orElseThrow().status());
    }

    @Test
    void publishedQuestEventUsesCanonicalExecutionAndEcopoints() {
        for (int i = 0; i < 2; i++)
            tx(
                    () ->
                            events.publishEvent(
                                    new QuestCompletedIntegrationEvent(
                                            UUID.randomUUID(),
                                            75L,
                                            41L,
                                            41L,
                                            1,
                                            USER.value(),
                                            WATER,
                                            DAILY_QUEST,
                                            2,
                                            13,
                                            AT.atOffset(ZoneOffset.UTC))));
        assertEquals(13, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(1, progress.getUserProgress(USER).getCurrentStreak());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void publishedQuestUsesEcopointsWithoutAnyAdditionalXpConfiguration() {
        tx(
                () ->
                        events.publishEvent(
                                new QuestCompletedIntegrationEvent(
                                        UUID.randomUUID(),
                                        75L,
                                        41L,
                                        41L,
                                        1,
                                        USER.value(),
                                        WATER,
                                        ACTIVITIES,
                                        2,
                                        13,
                                        AT.atOffset(ZoneOffset.UTC))));
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
        assertEquals(13, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(new Reward(13, 2), progress.getRecentRewards(USER).getFirst().grantedReward());
    }

    @Test
    void localRankingUsesVerifiedMembershipAndReadsDoNotWriteAwards() {
        assertEquals(1, rankings.participants(RankingType.LOCAL, USER, 0, 20).items().size());
        when(community.isMember(communityId, USER.value())).thenReturn(false);
        assertThrows(
                AccessDeniedException.class,
                () -> rankings.participants(RankingType.LOCAL, USER, 0, 20));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM achievement_awards", Integer.class));
    }

    @Test
    void invalidCanonicalCommunityIdsAreRejectedBeforeRewardsOrAwardsCanBeRecorded() {
        for (long id : new long[] {0, -1}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            new CommunityGoalCompletedIntegrationEvent(
                                    UUID.randomUUID(),
                                    UUID.randomUUID(),
                                    UUID.randomUUID(),
                                    id,
                                    List.of(USER.value()),
                                    null,
                                    AT));
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            new CommunityEventCompletedIntegrationEvent(
                                    UUID.randomUUID(),
                                    UUID.randomUUID(),
                                    id,
                                    List.of(USER.value()),
                                    null,
                                    AT));
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            new AchievementAward(
                                    UUID.randomUUID(),
                                    UUID.randomUUID(),
                                    AchievementScope.COMMUNITY,
                                    null,
                                    UUID.randomUUID(),
                                    AT,
                                    id));
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            new CommunityContextFacade.PublishAchievement(
                                    UUID.randomUUID(), UUID.randomUUID(), USER.value(), id));
        }
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM achievement_awards", Integer.class));
    }

    @Test
    void shareEndpointsUseAuthenticatedIdentityAndEnforceRequestOwnership() throws Exception {
        var award = unlock();
        var request = UUID.randomUUID();
        var payload =
                "{\"requestId\":\""
                        + request
                        + "\",\"awardId\":\""
                        + award.id()
                        + "\",\"communityId\":\""
                        + communityId
                        + "\"}";
        http.perform(
                        post("/api/v1/gamification/achievement-shares")
                                .contentType("application/json")
                                .content(payload))
                .andExpect(status().isUnauthorized());
        http.perform(
                        post("/api/v1/gamification/achievement-shares")
                                .header("Authorization", bearer(USER))
                                .contentType("application/json")
                                .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requestedBy").value(USER.value()));
        http.perform(
                        get("/api/v1/gamification/achievement-shares/" + request)
                                .header("Authorization", bearer(OTHER)))
                .andExpect(status().isForbidden());
        http.perform(
                        get("/api/v1/gamification/achievement-shares/" + request)
                                .header("Authorization", bearer(USER)))
                .andExpect(status().isOk());
    }

    @Test
    void rewardHistoryPeriodIsExclusiveAtEndAndRejectsOtherUsers() throws Exception {
        rewards.handle(quest(USER, AT, false, new Reward(7, 0)));
        rewards.handle(quest(USER, AT.plusSeconds(60), false, new Reward(8, 0)));
        var endpoint = "/api/v1/gamification/rewards";
        http.perform(
                        get(endpoint)
                                .param("from", AT.toString())
                                .param("to", AT.plusSeconds(60).toString())
                                .header("Authorization", bearer(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].grantedReward.ecopoints").value(7));
        http.perform(
                        get(endpoint)
                                .param("beneficiaryId", USER.value().toString())
                                .param("from", AT.toString())
                                .param("to", AT.plusSeconds(60).toString())
                                .header("Authorization", bearer(OTHER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void legacyRoutesPreserveTheTechnicalStoriesAndUserProfilesReadOwnedProgress()
            throws Exception {
        unlock();
        var auth = bearer(USER);
        for (var endpoint :
                List.of("/api/v1/achievement", "/api/v1/user_achievement", "/api/v1/ranking"))
            http.perform(get(endpoint).header("Authorization", auth)).andExpect(status().isOk());
        http.perform(get("/api/v1/user/" + USER.value()).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ecopoints").value(10));
        http.perform(
                        get("/api/v1/user_achievement")
                                .param("user_id", OTHER.value().toString())
                                .header("Authorization", auth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACHIEVEMENT_OWNER_REQUIRED"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.type").doesNotExist());
    }

    @Test
    void delayedPublicationAcknowledgmentKeepsOutboxPendingUntilConfirmation() {
        var award = unlock();
        var request = UUID.randomUUID();
        achievements.handle(
                new ShareAchievementCommand(request, award.id(), USER.value(), communityId));
        var message =
                outbox.findAll().stream()
                        .filter(m -> m.getMessageType().equals("SHARE_ACHIEVEMENT"))
                        .findFirst()
                        .orElseThrow();
        delivery.deliver(message.getId());
        assertNull(outbox.findById(message.getId()).orElseThrow().getDeliveredAt());
        achievements.handle(
                new ConfirmAchievementPublicationCommand(
                        request, award.id(), USER.value(), communityId, 94L));
        var row = outbox.findById(message.getId()).orElseThrow();
        row.setNextAttemptAt(Instant.EPOCH);
        outbox.save(row);
        delivery.deliver(message.getId());
        assertNotNull(outbox.findById(message.getId()).orElseThrow().getDeliveredAt());
        verify(community, times(1)).requestAchievementPublication(any());
    }

    @Test
    void configuredCosmeticRewardIsGrantedOnceUsingTheAwardIdentifier() {
        var cosmetic = LEAF_AVATAR;
        achievements.register(
                new Achievement(
                        UUID.randomUUID(),
                        "LEAF_GUARDIAN",
                        "Leaf guardian",
                        "Celebrate a greener routine",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        5,
                        true,
                        cosmetic));
        rewards.handle(quest(USER, AT, false, new Reward(5, 0)));
        rewards.handle(quest(USER, AT, false, new Reward(5, 0)));
        var message =
                outbox.findAll().stream()
                        .filter(m -> m.getMessageType().equals("COSMETIC"))
                        .findFirst()
                        .orElseThrow();
        delivery.deliver(message.getId());
        delivery.deliver(message.getId());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM user_cosmetics WHERE user_id=?",
                        Integer.class,
                        USER.value()));
        assertEquals(1, queries.forUser(USER, 0, 20).size());
    }

    @Test
    void realProtectorConsumptionResolvesTheDayWithoutDoubleInventoryDebit() {
        rewards.handle(quest(USER, AT, true, new Reward(10, 0)));
        var protector = STREAK_SHIELD;
        jdbc.update(
                "INSERT INTO protector_inventories (id,user_id,protector_id,quantity,version)"
                        + " VALUES (?,?,?,?,0)",
                UUID.randomUUID().toString(),
                USER.value(),
                protector.toString(),
                2);
        var request =
                protection.handle(
                        new RequestStreakProtectionCommand(
                                USER, DAY.plusDays(1), AT.plusSeconds(86400)));
        var message = outbox.findAll().getFirst();
        delivery.deliver(message.getId());
        delivery.deliver(message.getId());
        assertEquals(
                StreakProtectionStatus.PROTECTED,
                protections.find(USER, DAY.plusDays(1)).orElseThrow().status());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT quantity FROM protector_inventories WHERE user_id=?",
                        Integer.class,
                        USER.value()));
        assertEquals(DAY, progress.getUserProgress(USER).getLastActivityDate());
    }

    @Test
    void closureRecoversSeveralClosedDaysWithoutPretendingTheyWereDailyActivities() {
        rewards.handle(quest(USER, AT, true, new Reward(10, 0)));
        jdbc.update(
                "INSERT INTO protector_inventories (id,user_id,protector_id,quantity,version)"
                        + " VALUES (?,?,?,?,0)",
                UUID.randomUUID().toString(),
                USER.value(),
                STREAK_SHIELD.toString(),
                3);
        var closedThrough = DAY.plusDays(3);
        for (int day = 1; day <= 3; day++) {
            closure.closeThrough(closedThrough, AT.plusSeconds(4 * 86400));
            closure.closeThrough(closedThrough, AT.plusSeconds(4 * 86400));
            var message =
                    outbox.findAll().stream()
                            .filter(m -> m.getDeliveredAt() == null)
                            .findFirst()
                            .orElseThrow();
            delivery.deliver(message.getId());
            delivery.deliver(message.getId());
            assertEquals(
                    StreakProtectionStatus.PROTECTED,
                    protections.find(USER, DAY.plusDays(day)).orElseThrow().status());
            assertEquals(DAY, progress.getUserProgress(USER).getLastActivityDate());
            assertEquals(1, progress.getUserProgress(USER).getCurrentStreak());
        }
        closure.closeThrough(closedThrough, AT.plusSeconds(4 * 86400));
        assertEquals(3, outbox.count());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT quantity FROM protector_inventories WHERE user_id=?",
                        Integer.class,
                        USER.value()));
        rewards.handle(quest(USER, AT.plusSeconds(4 * 86400), true, new Reward(1, 0)));
        assertEquals(2, progress.getUserProgress(USER).getCurrentStreak());
        assertEquals(DAY.plusDays(4), progress.getUserProgress(USER).getLastActivityDate());
    }

    @Test
    void pagedRewardResourcePreservesSourceBeneficiaryAndBaseVersusEffectiveAmounts()
            throws Exception {
        UUID game = UUID.randomUUID();
        rewards.handle(
                new GrantMinigameRewardCommand(
                        UUID.randomUUID(), game, USER, AT, new Reward(100, 10)));
        rewards.handle(
                new GrantMinigameRewardCommand(
                        UUID.randomUUID(), game, USER, AT.plusSeconds(1), new Reward(100, 10)));
        http.perform(
                        get("/api/v1/gamification/rewards")
                                .param("from", AT.plusSeconds(1).toString())
                                .param("to", AT.plusSeconds(2).toString())
                                .param("size", "1")
                                .header("Authorization", bearer(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.items[0].source.type").value("MINIGAME"))
                .andExpect(jsonPath("$.items[0].source.executionId").isString())
                .andExpect(jsonPath("$.items[0].beneficiary.type").value("USER"))
                .andExpect(jsonPath("$.items[0].beneficiary.id").value(USER.value()))
                .andExpect(jsonPath("$.items[0].baseReward.ecopoints").value(100))
                .andExpect(jsonPath("$.items[0].grantedReward.ecopoints").value(80))
                .andExpect(jsonPath("$.items[0].grantedReward.experience").doesNotExist())
                .andExpect(jsonPath("$.items[0].grantedReward.gems").value(8));
        assertEquals(
                2, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
    }

    @Test
    void completionRollbackRemovesGrantScoreAwardAndOutboxTogether() {
        achievements.register(
                achievement(
                        "GREEN_STEPS",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        10));
        assertThrows(
                IllegalStateException.class,
                () ->
                        tx(
                                () -> {
                                    rewards.handle(quest(USER, AT, true, new Reward(10, 2)));
                                    throw new IllegalStateException("Completion rejected");
                                }));
        assertEquals(0, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
        assertEquals(0, queries.forUser(USER, 0, 20).size());
        assertEquals(0, outbox.count());
    }

    @Test
    void publishedMinigameUsesValidatedQuestsHistoryIncludingEarlierCompletions() {
        var attempts = applicationAttempts();
        tx(
                () ->
                        attempts.save(
                                new MinigameAttempt(
                                        null,
                                        USER.value(),
                                        41L,
                                        8L,
                                        20,
                                        COMPLETED,
                                        AT.minusSeconds(3600).atOffset(ZoneOffset.UTC),
                                        AT.minusSeconds(3500).atOffset(ZoneOffset.UTC),
                                        Map.of(),
                                        true)));
        for (int i = 0; i < 2; i++)
            tx(
                    () ->
                            events.publishEvent(
                                    new MinigameCompletedIntegrationEvent(
                                            UUID.randomUUID(),
                                            75L,
                                            41L,
                                            41L,
                                            1,
                                            8L,
                                            USER.value(),
                                            50,
                                            3,
                                            100,
                                            AT.atOffset(ZoneOffset.UTC))));
        assertEquals(80, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(0, progress.getUserProgress(USER).getCurrentStreak());
    }

    @Autowired MinigameAttemptRepository actualAttempts;

    private MinigameAttemptRepository applicationAttempts() {
        return actualAttempts;
    }

    private AchievementAward unlock() {
        achievements.register(
                achievement(
                        "GREEN_STEPS",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        10));
        rewards.handle(quest(USER, AT, false, new Reward(10, 0)));
        return queries.forUser(USER, 0, 20).getFirst();
    }

    private Achievement achievement(
            String code, AchievementScope scope, AchievementMetric metric, long target) {
        return new Achievement(
                UUID.randomUUID(),
                code,
                "Green steps",
                "A shared commitment to caring for nature",
                scope,
                metric,
                target,
                true);
    }

    @Test
    void concurrentMinigameAttemptsSerializeRepetitionAndRewardTotals() throws Exception {
        var game = UUID.randomUUID();
        var amounts =
                concurrently(
                        () ->
                                rewards.handle(
                                                new GrantMinigameRewardCommand(
                                                        UUID.randomUUID(),
                                                        game,
                                                        USER,
                                                        AT,
                                                        new Reward(100, 0)))
                                        .grantedReward()
                                        .ecopoints(),
                        () ->
                                rewards.handle(
                                                new GrantMinigameRewardCommand(
                                                        UUID.randomUUID(),
                                                        game,
                                                        USER,
                                                        AT,
                                                        new Reward(100, 0)))
                                        .grantedReward()
                                        .ecopoints());
        assertEquals(List.of(80L, 100L), amounts.stream().sorted().toList());
        assertEquals(180, progress.getUserProgress(USER).getTotalEcopoints());
    }

    @Test
    void concurrentShareRetriesCreateOneRequestAndOneOutboxMessage() throws Exception {
        var award = unlock();
        var command =
                new ShareAchievementCommand(
                        UUID.randomUUID(), award.id(), USER.value(), communityId);
        var results =
                concurrently(
                        () -> achievements.handle(command).toOptional().orElseThrow(),
                        () -> achievements.handle(command).toOptional().orElseThrow());
        assertEquals(results.getFirst(), results.getLast());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM achievement_share_requests", Integer.class));
        assertEquals(
                1,
                outbox.findAll().stream()
                        .filter(row -> row.getMessageType().equals("SHARE_ACHIEVEMENT"))
                        .count());
    }

    @Test
    void concurrentCommunityCompletionsDoNotLoseTheThresholdOrDuplicateAwards() throws Exception {
        achievements.register(
                achievement(
                        "COMMUNITY_PROGRESS",
                        AchievementScope.COMMUNITY,
                        AchievementMetric.COMPLETED_COMMUNITY_GOALS,
                        2));
        java.util.concurrent.Callable<Boolean> completion =
                () -> {
                    tx(
                            () ->
                                    events.publishEvent(
                                            new CommunityGoalCompletedIntegrationEvent(
                                                    UUID.randomUUID(),
                                                    UUID.randomUUID(),
                                                    UUID.randomUUID(),
                                                    communityId,
                                                    List.of(USER.value()),
                                                    null,
                                                    AT)));
                    return true;
                };
        concurrently(completion, completion);
        assertEquals(
                2,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM achievement_milestones WHERE beneficiary=?",
                        Integer.class,
                        "COMMUNITY:" + communityId));
        assertEquals(
                1,
                queries.forCommunity(communityId, USER, 0, 20).toOptional().orElseThrow().size());
    }

    @Test
    void databaseRejectsOrphanSharesInvalidResolutionAndNegativeProgress() {
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "INSERT INTO achievement_share_requests"
                                    + " (id,award_id,requested_by,community_id,status,created_at,version)"
                                    + " VALUES (?,?,?,?,?,?,0)",
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                USER.value(),
                                communityId.toString(),
                                "PENDING",
                                java.sql.Timestamp.from(AT)));
        rewards.handle(quest(USER, AT, true, new Reward(1, 0)));
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "UPDATE user_progresses SET total_ecopoints=-1 WHERE user_id=?",
                                USER.value()));
        var request =
                protection.handle(
                        new RequestStreakProtectionCommand(USER, DAY.plusDays(1), Instant.now()));
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "UPDATE streak_protection_requests SET status='PROTECTED' WHERE"
                                        + " id=?",
                                request.id().toString()));
    }

    private <T> List<T> concurrently(
            java.util.concurrent.Callable<T> first, java.util.concurrent.Callable<T> second)
            throws Exception {
        var gate = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a =
                    executor.submit(
                            () -> {
                                gate.await();
                                return first.call();
                            });
            var b =
                    executor.submit(
                            () -> {
                                gate.await();
                                return second.call();
                            });
            gate.countDown();
            return List.of(
                    a.get(20, java.util.concurrent.TimeUnit.SECONDS),
                    b.get(20, java.util.concurrent.TimeUnit.SECONDS));
        }
    }

    @Test
    void publishedMinigameHttpWorkflowUsesEcopointsAndCreditsRealWallet() throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var gameResponse =
                http.perform(
                                post("/api/v1/minigames")
                                        .header("Authorization", bearer(USER))
                                        .contentType("application/json")
                                        .content(
                                                """
                                                {"name":"Forest Trail","description":"Collect the scattered bottles.","url":"/games/forest-trail","completionRules":{"minScore":50}}
                                                """))
                        .andExpect(status().isCreated())
                        .andReturn();
        long gameId =
                mapper.readTree(gameResponse.getResponse().getContentAsString()).get("id").asLong();
        var questResponse =
                http.perform(
                                post("/api/v1/quests")
                                        .header("Authorization", bearer(USER))
                                        .contentType("application/json")
                                        .content(
                                                """
                                                {"minigameId":%d,"title":"Clean the trail","description":"Collect and recycle the bottles.","category":"WATER","type":"MINIGAME","gemReward":4,"ecopoints":19,"age":9,"time":5,"theme":"MINIGAME","image":"https://example.net/trail.png"}
                                                """
                                                        .formatted(gameId)))
                        .andExpect(status().isCreated())
                        .andReturn();
        var created = mapper.readTree(questResponse.getResponse().getContentAsString());
        long questId = created.get("id").asLong();
        assertEquals(questId, created.get("versionGroupId").asLong());
        http.perform(
                        patch("/api/v1/quests/" + questId + "/publish")
                                .header("Authorization", bearer(USER)))
                .andExpect(status().isOk());
        var attemptResponse =
                http.perform(
                                post("/api/v1/minigame-attempts")
                                        .header("Authorization", bearer(USER))
                                        .contentType("application/json")
                                        .content(
                                                "{\"userId\":%d,\"questId\":%d}"
                                                        .formatted(USER.value(), questId)))
                        .andExpect(status().isCreated())
                        .andReturn();
        long attemptId =
                mapper.readTree(attemptResponse.getResponse().getContentAsString())
                        .get("id")
                        .asLong();
        http.perform(
                        post("/api/v1/minigame-attempts/" + attemptId + "/finish")
                                .header("Authorization", bearer(USER))
                                .contentType("application/json")
                                .content("{\"score\":80,\"metadata\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        assertEquals(19, progress.getUserProgress(USER).getTotalEcopoints());
        assertEquals(0, progress.getUserProgress(USER).getCurrentStreak());
        for (var message : outbox.findAll())
            if (message.getMessageType().equals("GEMS")) delivery.deliver(message.getId());
        assertEquals(
                4,
                jdbc.queryForObject(
                        "SELECT gem_balance FROM user_profiles WHERE user_id=?",
                        Integer.class,
                        USER.value()));
        http.perform(
                        post("/api/v1/minigame-attempts/" + attemptId + "/finish")
                                .header("Authorization", bearer(USER))
                                .contentType("application/json")
                                .content("{\"score\":80,\"metadata\":{}}"))
                .andExpect(status().isUnprocessableEntity());
        assertEquals(1, progress.getRecentRewards(USER).size());
    }

    private GrantQuestRewardCommand quest(UserId user, Instant at, boolean daily, Reward reward) {
        return new GrantQuestRewardCommand(
                UUID.randomUUID(),
                user,
                at,
                at.atZone(ZoneId.of("America/Lima")).toLocalDate(),
                daily,
                reward);
    }

    private void tx(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(s -> action.run());
    }

    private String bearer(UserId user) {
        return "Bearer "
                + tokens.issueAccessToken(
                                new AuthenticatedUser(
                                        new AccountId(user.value()),
                                        new EmailAddress("lucia.rios@example.net")))
                        .value();
    }
}
