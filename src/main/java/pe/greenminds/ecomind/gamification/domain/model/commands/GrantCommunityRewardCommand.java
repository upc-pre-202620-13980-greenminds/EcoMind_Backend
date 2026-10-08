package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record GrantCommunityRewardCommand(
        RewardSourceType sourceType,
        UUID sourceExecutionId,
        List<UserId> participants,
        Instant occurredAt,
        Reward baseReward) {
    public GrantCommunityRewardCommand {
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(baseReward);
        participants = List.copyOf(participants);
        if (participants.isEmpty()
                || participants.stream().distinct().count() != participants.size())
            throw new IllegalArgumentException("Participants must be nonempty and unique");
        if (sourceType != RewardSourceType.COMMUNITY_GOAL
                && sourceType != RewardSourceType.COMMUNITY_EVENT)
            throw new IllegalArgumentException("Invalid community source");
    }
}
