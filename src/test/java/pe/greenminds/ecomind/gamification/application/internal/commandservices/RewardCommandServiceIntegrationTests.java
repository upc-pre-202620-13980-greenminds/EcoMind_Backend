package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_AND_AFTER_CLASS)
class RewardCommandServiceIntegrationTests {
    @Autowired private RewardCommandService rewards;
    @Autowired private GamificationQueryService queries;
    @Autowired private RewardTransactionPersistenceRepository rewardRows;
    @Autowired private UserProgressPersistenceRepository progressRows;

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
}
