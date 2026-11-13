package pe.greenminds.ecomind.community.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Notice for the other bounded contexts: a community goal was completed. Unlike
 * CommunityGoalCompletedIntegrationEvent it carries no reward, so it never grants anything.
 */
public record CommunityGoalReachedIntegrationEvent(
        UUID eventId, Long goalId, Long communityId, String title, Instant occurredAt) {

    public CommunityGoalReachedIntegrationEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(goalId);
        Objects.requireNonNull(communityId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(occurredAt);
    }
}
