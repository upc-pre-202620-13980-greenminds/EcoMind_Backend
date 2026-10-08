package pe.greenminds.ecomind.community.interfaces.acl.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Contract only. A null configuredReward means the goal has no monetary/point reward configured.
 */
public record CommunityGoalCompletedIntegrationEvent(
        UUID eventId,
        UUID executionId,
        UUID goalId,
        Long communityId,
        List<Long> eligibleParticipantIds,
        ConfiguredReward configuredReward,
        Instant occurredAt) {
    public record ConfiguredReward(long ecopoints, int gems) {
        public ConfiguredReward {
            if (ecopoints < 0 || gems < 0)
                throw new IllegalArgumentException("Invalid reward amounts");
        }
    }

    public CommunityGoalCompletedIntegrationEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(executionId);
        Objects.requireNonNull(goalId);
        Objects.requireNonNull(communityId);
        if (communityId <= 0) throw new IllegalArgumentException("Community id must be positive");
        Objects.requireNonNull(occurredAt);
        eligibleParticipantIds = List.copyOf(eligibleParticipantIds);
        if (eligibleParticipantIds.stream().anyMatch(id -> id <= 0)
                || eligibleParticipantIds.stream().distinct().count()
                        != eligibleParticipantIds.size())
            throw new IllegalArgumentException("Eligible participants must be unique positive ids");
    }
}
