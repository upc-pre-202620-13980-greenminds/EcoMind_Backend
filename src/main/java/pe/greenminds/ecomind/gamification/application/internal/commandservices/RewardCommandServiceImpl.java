package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.UserProgressCommandService;
import pe.greenminds.ecomind.gamification.application.internal.services.MinigameRepetitionPolicy;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.application.outboundservices.MonetizationServiceClient;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCollaborativeQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCommunityRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantMinigameRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserScoreCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateUserStreakCommand;
import pe.greenminds.ecomind.gamification.domain.model.events.RewardGrantedEvent;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.ActiveMultiplier;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.MinigameCompletionRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardTransactionRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;
import pe.greenminds.ecomind.gamification.domain.services.RewardCalculationService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RewardCommandServiceImpl implements RewardCommandService {
    private final UserProgressRepository progressRepository;
    private final RewardTransactionRepository rewardRepository;
    private final MonetizationServiceClient monetization;
    private final GamificationEventPublisher events;
    private final MinigameCompletionRepository completions;
    private final MinigameRepetitionPolicy repetition;
    private final UserProgressCommandService userCommands;
    private final RewardCalculationService calculation = new RewardCalculationService();

    public RewardCommandServiceImpl(
            UserProgressRepository progressRepository,
            RewardTransactionRepository rewardRepository,
            MonetizationServiceClient monetization,
            GamificationEventPublisher events,
            MinigameCompletionRepository completions,
            MinigameRepetitionPolicy repetition,
            UserProgressCommandService userCommands) {
        this.progressRepository = progressRepository;
        this.rewardRepository = rewardRepository;
        this.monetization = monetization;
        this.events = events;
        this.completions = completions;
        this.repetition = repetition;
        this.userCommands = userCommands;
    }

    public RewardTransaction handle(GrantQuestRewardCommand c) {
        return grant(
                RewardSourceType.QUEST,
                c.sourceExecutionId(),
                c.userId(),
                c.occurredAt(),
                c.activityDate(),
                c.countsForDailyStreak(),
                c.baseReward(),
                BigDecimal.ONE);
    }

    public RewardTransaction handle(GrantMinigameRewardCommand c) {
        progressRepository.lockForReward(c.userId());
        var prior =
                rewardRepository.findByOrigin(
                        RewardSourceType.MINIGAME, c.sourceExecutionId(), c.userId());
        if (prior.isPresent()) return prior.get();
        long local =
                completions.count(
                        c.userId(),
                        c.minigameId(),
                        c.occurredAt().minus(Duration.ofHours(3)),
                        c.occurredAt());
        var factor =
                repetition.factor(
                        c.validatedPriorAttempts() == null
                                ? local
                                : Math.max(local, c.validatedPriorAttempts()));
        var result =
                grant(
                        RewardSourceType.MINIGAME,
                        c.sourceExecutionId(),
                        c.userId(),
                        c.occurredAt(),
                        null,
                        false,
                        c.baseReward(),
                        factor);
        completions.record(c.sourceExecutionId(), c.userId(), c.minigameId(), c.occurredAt());
        return result;
    }

    public List<RewardTransaction> handle(GrantCollaborativeQuestRewardCommand c) {
        return c.participants().stream()
                .sorted(Comparator.comparing(UserId::value))
                .map(
                        user ->
                                grant(
                                        RewardSourceType.COLLABORATIVE_QUEST,
                                        c.sourceExecutionId(),
                                        user,
                                        c.occurredAt(),
                                        null,
                                        false,
                                        c.baseReward(),
                                        BigDecimal.ONE))
                .toList();
    }

    public List<RewardTransaction> handle(GrantCommunityRewardCommand c) {
        return c.participants().stream()
                .sorted(Comparator.comparing(UserId::value))
                .map(
                        user ->
                                grant(
                                        c.sourceType(),
                                        c.sourceExecutionId(),
                                        user,
                                        c.occurredAt(),
                                        null,
                                        false,
                                        c.baseReward(),
                                        BigDecimal.ONE))
                .toList();
    }

    private RewardTransaction grant(
            RewardSourceType type,
            UUID execution,
            UserId user,
            Instant at,
            LocalDate date,
            boolean daily,
            Reward base,
            BigDecimal factor) {
        var progress = progressRepository.lockForReward(user);
        var prior = rewardRepository.findByOrigin(type, execution, user);
        if (prior.isPresent()) return prior.get();
        // Supplier errors propagate: absence of a response is not proof of an inactive multiplier.
        var multiplier =
                base.ecopoints() == 0
                        ? Optional.<ActiveMultiplier>empty()
                        : monetization
                                .getActiveMultiplier(user.value(), at)
                                .map(
                                        m ->
                                                new ActiveMultiplier(
                                                        m.id(),
                                                        m.experienceFactor(),
                                                        m.startsAt(),
                                                        m.expiresAt()));
        var granted = calculation.calculate(base, multiplier, at, factor);
        var transaction =
                new RewardTransaction(
                        UUID.randomUUID(),
                        type,
                        execution,
                        user,
                        base,
                        granted,
                        at,
                        multiplier
                                .filter(m -> m.isActiveAt(at))
                                .map(ActiveMultiplier::id)
                                .orElse(null),
                        factor.multiply(
                                multiplier
                                        .filter(m -> m.isActiveAt(at))
                                        .map(ActiveMultiplier::factor)
                                        .orElse(BigDecimal.ONE)),
                        factor);
        rewardRepository.save(transaction);
        userCommands.handle(new UpdateUserScoreCommand(user, granted, execution, at));
        if (daily) userCommands.handle(new UpdateUserStreakCommand(user, date, execution, at));
        events.publish(new RewardGrantedEvent(transaction));
        return transaction;
    }
}
