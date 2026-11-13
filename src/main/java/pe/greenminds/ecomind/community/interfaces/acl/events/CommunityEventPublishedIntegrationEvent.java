package pe.greenminds.ecomind.community.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Notice for the other bounded contexts: a community event was created or its details changed.
 * It does not change registrations, places or rewards.
 */
public record CommunityEventPublishedIntegrationEvent(
        UUID eventId,
        Long communityEventId,
        Long communityId,
        Long authorId,
        String name,
        Action action,
        Instant occurredAt) {

    public enum Action {
        CREATED,
        UPDATED
    }

    public CommunityEventPublishedIntegrationEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(communityEventId);
        Objects.requireNonNull(communityId);
        Objects.requireNonNull(name);
        Objects.requireNonNull(action);
        Objects.requireNonNull(occurredAt);
    }
}
