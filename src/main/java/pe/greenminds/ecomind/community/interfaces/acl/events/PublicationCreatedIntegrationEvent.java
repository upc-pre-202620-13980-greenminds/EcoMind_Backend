package pe.greenminds.ecomind.community.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Correlated confirmation after the post is persisted; sending a request is not this confirmation. */
public record PublicationCreatedIntegrationEvent(UUID eventId, UUID requestId, UUID awardId,
    Long requestedBy, UUID communityId, UUID publicationId, Instant occurredAt) {
  public PublicationCreatedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(requestId); Objects.requireNonNull(awardId);
    Objects.requireNonNull(communityId); Objects.requireNonNull(publicationId); Objects.requireNonNull(occurredAt);
    if (requestedBy == null || requestedBy <= 0) throw new IllegalArgumentException("Requester must be positive");
  }
}
