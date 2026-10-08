package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** A completion fact already validated by Quests, not a request from a mobile client. */
public record GrantQuestRewardCommand(
        UUID sourceExecutionId,
        UserId userId,
        Instant occurredAt,
        LocalDate activityDate,
        boolean countsForDailyStreak,
        Reward baseReward) {

    public GrantQuestRewardCommand {
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(activityDate);
        Objects.requireNonNull(baseReward);
    }
}
