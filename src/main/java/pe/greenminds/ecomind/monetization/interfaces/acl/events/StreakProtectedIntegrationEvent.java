package pe.greenminds.ecomind.monetization.interfaces.acl.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Emitted only after the inventory debit commits; requestId correlates the original risk request. */
public record StreakProtectedIntegrationEvent(UUID eventId, UUID requestId, Long userId,
    LocalDate streakDate, Instant occurredAt) {
  public StreakProtectedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(requestId);
    Objects.requireNonNull(streakDate); Objects.requireNonNull(occurredAt);
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
  }
}
