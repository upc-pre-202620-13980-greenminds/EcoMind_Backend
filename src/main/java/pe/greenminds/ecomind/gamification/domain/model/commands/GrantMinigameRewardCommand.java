package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record GrantMinigameRewardCommand(
        UUID sourceExecutionId,
        UUID minigameId,
        UserId userId,
        Instant occurredAt,
        Reward baseReward,
        Long validatedPriorAttempts) {
    public GrantMinigameRewardCommand(
            UUID sourceExecutionId,
            UUID minigameId,
            UserId userId,
            Instant occurredAt,
            Reward baseReward) {
        this(sourceExecutionId, minigameId, userId, occurredAt, baseReward, null);
    }

    public GrantMinigameRewardCommand {
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(baseReward);
        Objects.requireNonNull(minigameId);
        Objects.requireNonNull(userId);
        if (validatedPriorAttempts != null && validatedPriorAttempts < 0)
            throw new IllegalArgumentException("Invalid prior attempt count");
    }
}
