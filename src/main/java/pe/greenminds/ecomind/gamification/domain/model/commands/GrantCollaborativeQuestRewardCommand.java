package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record GrantCollaborativeQuestRewardCommand(
        UUID sourceExecutionId, List<UserId> participants, Instant occurredAt, Reward baseReward) {
    public GrantCollaborativeQuestRewardCommand {
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(baseReward);
        participants = List.copyOf(participants);
        if (participants.isEmpty()
                || participants.stream().distinct().count() != participants.size())
            throw new IllegalArgumentException("Participants must be nonempty and unique");
    }
}
