package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.UserProgressCommandService;
import pe.greenminds.ecomind.gamification.application.internal.commandservices.AchievementCatalogSeed;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCollaborativeQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantMinigameRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserScoreCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.*;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardTransactionRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FirstQuestAchievementIntegrationTests {
    private static final UserId USER = new UserId(12001L);
    private static final UserId OTHER = new UserId(12002L);
    private static final Instant AT = Instant.parse("2026-11-13T15:00:00Z");
    private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

    @Autowired AchievementRepository catalog;
    @Autowired AchievementAwardRepository awards;
    @Autowired AchievementCommandService achievements;
    @Autowired RewardCommandService rewards;
    @Autowired RewardTransactionRepository rewardRows;
    @Autowired UserProgressRepository progress;
    @Autowired UserProgressCommandService scores;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ApplicationEventPublisher events;

    @BeforeEach
    void prepare() {
        for (var table : List.of("\"gamification_outbox\"", "achievement_share_requests",
                "achievement_awards", "achievements", "gamification_minigame_completions",
                "reward_transactions", "user_progresses")) {
            jdbc.update("DELETE FROM " + table);
        }
        seed();
    }

    @ParameterizedTest
    @EnumSource(value = RewardSourceType.class, names = {"QUEST", "MINIGAME", "COLLABORATIVE_QUEST"})
    void firstSuccessfulCompletionEarnsOneMedalEvenWithoutPoints(RewardSourceType type) {
        var execution = UUID.randomUUID();
        complete(type, execution);
        complete(type, execution);
        complete(type, UUID.randomUUID());

        var earned = awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100);
        assertEquals(1, earned.size());
        assertEquals("FIRST_QUEST_COMPLETED", catalog.findById(earned.getFirst().achievementId())
                .orElseThrow().code());
        assertEquals(0, progress.findByUserId(USER).orElseThrow().getTotalEcopoints());
        assertEquals(2, rewardRows.countCompletedQuests(USER));
        assertTrue(awards.findByBeneficiary(AchievementScope.INDIVIDUAL, OTHER.value(), 0, 100).isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = RewardSourceType.class, names = {"COMMUNITY_GOAL", "COMMUNITY_EVENT", "STREAK_BONUS"})
    void pointsFromOtherSourcesDoNotCountAsACompletedQuest(RewardSourceType type) {
        tx(() -> {
            progress.lockForReward(USER);
            var execution = UUID.randomUUID();
            var reward = new Reward(20, 0);
            rewardRows.save(new RewardTransaction(UUID.randomUUID(), type, execution, USER, reward, reward, AT));
            scores.handle(new UpdateUserScoreCommand(USER, reward, execution, AT));
        });
        assertEquals(0, rewardRows.countCompletedQuests(USER));
        assertTrue(awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).isEmpty());
    }

    @Test
    void failedTransactionDoesNotLeaveAMedal() {
        var execution = UUID.randomUUID();
        assertThrows(IllegalStateException.class, () -> tx(() -> {
            complete(RewardSourceType.QUEST, execution);
            throw new IllegalStateException("Completion failed");
        }));
        assertEquals(0, rewardRows.countCompletedQuests(USER));
        assertTrue(awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).isEmpty());
        complete(RewardSourceType.QUEST, execution);
        assertEquals(1, awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).size());
    }

    @Test
    void publishedZeroPointCompletionUnlocksTheFirstQuestMedalOnlyOnce() {
        var completedAt = AT.atOffset(java.time.ZoneOffset.UTC);
        tx(() -> events.publishEvent(new QuestCompletedIntegrationEvent(
                UUID.randomUUID(), 901L, 101L, 101L, 1, USER.value(),
                Category.WATER, QuestType.ACTIVITIES, 0, 0, completedAt)));
        tx(() -> events.publishEvent(new QuestCompletedIntegrationEvent(
                UUID.randomUUID(), 901L, 101L, 101L, 1, USER.value(),
                Category.WATER, QuestType.ACTIVITIES, 0, 0, completedAt)));

        assertEquals(1, rewardRows.countCompletedQuests(USER));
        assertEquals(1, awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).size());
    }

    @Test
    void concurrentCompletionsDoNotDuplicateTheMedal() throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                start.await();
                complete(RewardSourceType.QUEST, UUID.randomUUID());
                return null;
            });
            var second = executor.submit(() -> {
                start.await();
                complete(RewardSourceType.QUEST, UUID.randomUUID());
                return null;
            });
            start.countDown();
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        }
        assertEquals(2, rewardRows.countCompletedQuests(USER));
        assertEquals(1, awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).size());
    }

    @Test
    void retiringTheOldDefaultPreservesEarnedMedalsAndCustomDefinitions() {
        var legacyId = UUID.nameUUIDFromBytes("ecomind:achievement:FIRST_ECOPOINTS".getBytes(StandardCharsets.UTF_8));
        achievements.register(new Achievement(legacyId, "FIRST_ECOPOINTS", "First ecopoints",
                "Earn one ecopoint", AchievementScope.INDIVIDUAL, AchievementMetric.ECOPOINTS, 1, true));
        tx(() -> scores.handle(new UpdateUserScoreCommand(USER, new Reward(1, 0), UUID.randomUUID(), AT)));
        var existing = awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100);
        assertEquals(1, existing.size());

        seed();
        seed();

        assertFalse(catalog.findById(legacyId).orElseThrow().active());
        assertEquals(existing, awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100));
        complete(RewardSourceType.QUEST, UUID.randomUUID());
        assertEquals(2, awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).size());
    }

    @Test
    void existingCompletedQuestsReceiveTheRestoredMedalOnUpgrade() {
        jdbc.update("DELETE FROM achievements WHERE code = 'FIRST_QUEST_COMPLETED'");
        tx(() -> {
            progress.lockForReward(USER);
            rewardRows.save(new RewardTransaction(UUID.randomUUID(), RewardSourceType.QUEST,
                    UUID.randomUUID(), USER, new Reward(0, 0), new Reward(0, 0), AT));
        });
        seed();
        seed();

        assertEquals(1, rewardRows.countCompletedQuests(USER));
        assertEquals(1, awards.findByBeneficiary(AchievementScope.INDIVIDUAL, USER.value(), 0, 100).size());
        assertEquals(0, progress.findByUserId(USER).orElseThrow().getTotalEcopoints());
    }

    private void complete(RewardSourceType type, UUID execution) {
        var reward = new Reward(0, 0);
        switch (type) {
            case QUEST -> rewards.handle(new GrantQuestRewardCommand(execution, USER, AT, DAY, true, reward));
            case MINIGAME -> rewards.handle(new GrantMinigameRewardCommand(execution, UUID.randomUUID(), USER, AT, reward));
            case COLLABORATIVE_QUEST -> rewards.handle(new GrantCollaborativeQuestRewardCommand(execution, List.of(USER), AT, reward));
            default -> throw new IllegalArgumentException("Not a quest completion");
        }
    }

    private void seed() {
        tx(() -> new AchievementCatalogSeed(catalog, achievements, rewardRows)
                .run(new DefaultApplicationArguments(new String[0])));
    }

    private void tx(Runnable work) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> work.run());
    }
}
