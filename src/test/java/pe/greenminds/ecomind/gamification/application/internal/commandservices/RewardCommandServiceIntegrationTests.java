package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.RewardSourceSchemaUpgrade;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.RewardTransactionPersistenceEntity;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.GamificationOutboxPersistenceRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.RewardTransactionPersistenceRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.UserProgressPersistenceRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RewardCommandServiceIntegrationTests {
    @Autowired private RewardCommandService rewards;
    @Autowired private GamificationQueryService queries;
    @Autowired private RewardTransactionPersistenceRepository rewardRows;
    @Autowired private UserProgressPersistenceRepository progressRows;
    @Autowired private UserProgressRepository progress;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private JdbcTemplate jdbc;

    private static final LocalDate FIRST_DAY = LocalDate.of(2026, 10, 1);
    private static final UserId STREAK_USER = new UserId(501L);

    @BeforeEach
    void clean() {
        rewardRows.deleteAll();
        progressRows.deleteAll();
    }

    @Test
    void repeatedExecutionReturnsOriginalGrantAndDoesNotAddPointsAgain() {
        var executionId = UUID.randomUUID();
        var userId = new UserId(101L);
        var command =
                new GrantQuestRewardCommand(
                        executionId,
                        userId,
                        Instant.parse("2026-10-05T15:00:00Z"),
                        LocalDate.of(2026, 10, 5),
                        true,
                        new Reward(15, 0));

        var original = rewards.handle(command);
        var duplicate = rewards.handle(command);

        assertEquals(original.id(), duplicate.id());
        assertEquals(1, rewardRows.count());
        assertEquals(15, queries.getUserProgress(userId).getTotalEcopoints());
        assertEquals(1, queries.getUserProgress(userId).getCurrentStreak());
    }

    @Test
    void sameExecutionCanRewardDifferentBeneficiariesAndIndependentDailyExecutions() {
        var executionId = UUID.randomUUID();
        var monday = LocalDate.of(2026, 10, 5);
        var userOne = new UserId(201L);
        var userTwo = new UserId(202L);
        rewards.handle(
                new GrantQuestRewardCommand(
                        executionId, userOne, Instant.now(), monday, true, new Reward(10, 0)));
        rewards.handle(
                new GrantQuestRewardCommand(
                        UUID.randomUUID(),
                        userOne,
                        Instant.now(),
                        monday,
                        true,
                        new Reward(10, 0)));
        rewards.handle(
                new GrantQuestRewardCommand(
                        executionId, userTwo, Instant.now(), monday, true, new Reward(10, 0)));

        assertEquals(3, rewardRows.count());
        assertEquals(20, queries.getUserProgress(userOne).getTotalEcopoints());
        assertEquals(1, queries.getUserProgress(userOne).getCurrentStreak());
        assertEquals(10, queries.getUserProgress(userTwo).getTotalEcopoints());
        assertEquals(2, queries.getRecentRewards(userOne).size());
    }

    @Autowired private GamificationOutboxPersistenceRepository outbox;

    @Test
    void gemRewardIsRecordedWithDurableDeliveryWithoutPartialWalletWrites() {
        var userId = new UserId(301L);
        var execution = UUID.randomUUID();
        var grant =
                rewards.handle(
                        new GrantQuestRewardCommand(
                                execution,
                                userId,
                                Instant.now(),
                                LocalDate.of(2026, 10, 5),
                                true,
                                new Reward(10, 1)));
        assertEquals(1, rewardRows.count());
        assertEquals(10, queries.getUserProgress(userId).getTotalEcopoints());
        assertEquals(
                1,
                outbox.findAll().stream()
                        .filter(row -> row.getCorrelationId().equals(grant.id().toString()))
                        .count());
    }

    @Test
    void simultaneousDeliveriesForTheSameUserCommitOnlyOneGrant() throws Exception {
        var userId = new UserId(401L);
        var command =
                new GrantQuestRewardCommand(
                        UUID.randomUUID(),
                        userId,
                        Instant.now(),
                        LocalDate.of(2026, 10, 5),
                        true,
                        new Reward(12, 0));
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () -> {
                                start.await();
                                return rewards.handle(command);
                            });
            var second =
                    executor.submit(
                            () -> {
                                start.await();
                                return rewards.handle(command);
                            });
            start.countDown();
            assertEquals(
                    first.get(10, TimeUnit.SECONDS).id(), second.get(10, TimeUnit.SECONDS).id());
        }
        assertEquals(1, rewardRows.count());
        assertEquals(12, queries.getUserProgress(userId).getTotalEcopoints());
    }

    @ParameterizedTest
    @CsvSource({"6,60,0", "7,77,1", "14,175,2", "21,308,3", "28,462,4"})
    void weeklyBonusesFollowTheOriginalRule(int days, long points, int bonuses) {
        completeDays(days);

        assertEquals(points, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
        assertEquals(days, queries.getUserProgress(STREAK_USER).getCurrentStreak());
        assertEquals(bonuses, bonusCount());
        rewardRows.findAll().stream().filter(row -> row.getSourceType().equals("STREAK_BONUS"))
                .forEach(row -> {
                    assertEquals(0, row.getGems());
                    assertEquals(0, row.getBaseGems());
                    assertEquals(row.getBaseEcopoints(), row.getEcopoints());
                    assertEquals(1, row.getAppliedFactor().intValueExact());
                });
    }

    @Test
    void retryAndAnotherActivityOnTheMilestoneDayDoNotRepeatTheBonus() {
        completeDays(6);
        var seventh = daily(FIRST_DAY.plusDays(6));
        rewards.handle(seventh);
        rewards.handle(seventh);
        rewards.handle(daily(FIRST_DAY.plusDays(6)));
        rewards.handle(daily(FIRST_DAY.plusDays(4)));

        assertEquals(1, bonusCount());
        assertEquals(7, queries.getUserProgress(STREAK_USER).getCurrentStreak());
        assertEquals(97, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
    }

    @Test
    void concurrentMilestoneActivitiesGrantOneWeeklyBonus() throws Exception {
        completeDays(6);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                start.await();
                return rewards.handle(daily(FIRST_DAY.plusDays(6)));
            });
            var second = executor.submit(() -> {
                start.await();
                return rewards.handle(daily(FIRST_DAY.plusDays(6)));
            });
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        assertEquals(1, bonusCount());
        assertEquals(7, queries.getUserProgress(STREAK_USER).getCurrentStreak());
        assertEquals(87, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
    }

    @Test
    void failedMilestoneRollsBackBothTheBonusAndTheStreak() {
        completeDays(6);
        assertThrows(IllegalStateException.class, () ->
                new TransactionTemplate(transactions).executeWithoutResult(status -> {
                    rewards.handle(daily(FIRST_DAY.plusDays(6)));
                    throw new IllegalStateException("Completion failed");
                }));

        assertEquals(0, bonusCount());
        assertEquals(6, queries.getUserProgress(STREAK_USER).getCurrentStreak());
        assertEquals(60, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
        rewards.handle(daily(FIRST_DAY.plusDays(6)));
        assertEquals(1, bonusCount());
        assertEquals(77, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
    }

    @Test
    void brokenStreakRestartsTheWeeklyBonus() {
        completeDays(7);
        for (int day = 8; day < 15; day++) rewards.handle(daily(FIRST_DAY.plusDays(day)));

        assertEquals(2, bonusCount());
        assertEquals(7, queries.getUserProgress(STREAK_USER).getCurrentStreak());
        assertEquals(154, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
    }

    @Test
    void protectionPreservesProgressButOnlyAnActivityGrantsTheBonus() {
        completeDays(6);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            var user = progress.lockForReward(STREAK_USER);
            user.protect(FIRST_DAY.plusDays(6));
            progress.save(user);
        });
        assertEquals(0, bonusCount());
        rewards.handle(daily(FIRST_DAY.plusDays(7)));

        assertEquals(1, bonusCount());
        assertEquals(7, queries.getUserProgress(STREAK_USER).getCurrentStreak());
        assertEquals(77, queries.getUserProgress(STREAK_USER).getTotalEcopoints());
    }

    @Test
    void existingPostgresConstraintCanBeUpgradedRepeatedly() {
        assumeTrue(jdbc.execute((java.sql.Connection connection) ->
                connection.getMetaData().getDatabaseProductName()).equals("PostgreSQL"));
        jdbc.execute("ALTER TABLE reward_transactions DROP CONSTRAINT ck_rewardtransaction");
        jdbc.execute("ALTER TABLE reward_transactions ADD CONSTRAINT ck_rewardtransaction CHECK ("
                + RewardTransactionPersistenceEntity.CHECK_CONSTRAINT.replace(",'STREAK_BONUS'", "") + ")");
        var upgrade = new RewardSourceSchemaUpgrade(jdbc);
        upgrade.afterPropertiesSet();
        upgrade.afterPropertiesSet();
        completeDays(7);

        assertEquals(1, bonusCount());
    }

    private void completeDays(int days) {
        for (int day = 0; day < days; day++) rewards.handle(daily(FIRST_DAY.plusDays(day)));
    }

    private GrantQuestRewardCommand daily(LocalDate date) {
        return new GrantQuestRewardCommand(UUID.randomUUID(), STREAK_USER,
                date.atTime(17, 0).toInstant(java.time.ZoneOffset.UTC), date, true, new Reward(10, 0));
    }

    private long bonusCount() {
        return rewardRows.findAll().stream()
                .filter(row -> row.getSourceType().equals(RewardSourceType.STREAK_BONUS.name())).count();
    }
}
