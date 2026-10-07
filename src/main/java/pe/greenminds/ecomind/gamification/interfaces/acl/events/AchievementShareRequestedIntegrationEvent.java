package pe.greenminds.ecomind.gamification.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Voluntary request: emit only after validating award ownership and target community membership. */
public record AchievementShareRequestedIntegrationEvent(UUID eventId, UUID requestId, UUID awardId,
    Long requestedBy, UUID communityId, Instant occurredAt) {
  public AchievementShareRequestedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(requestId); Objects.requireNonNull(awardId);
    Objects.requireNonNull(communityId); Objects.requireNonNull(occurredAt);
    if (requestedBy == null || requestedBy <= 0) throw new IllegalArgumentException("Requester must be positive");
  }
}
