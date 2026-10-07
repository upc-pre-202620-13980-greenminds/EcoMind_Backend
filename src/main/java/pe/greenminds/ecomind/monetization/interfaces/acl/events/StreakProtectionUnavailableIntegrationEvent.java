package pe.greenminds.ecomind.monetization.interfaces.acl.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Confirmed lack of inventory only. Timeouts and technical errors must remain retryable. */
public record StreakProtectionUnavailableIntegrationEvent(UUID eventId, UUID requestId, Long userId,
    LocalDate streakDate, Instant occurredAt) {
  public StreakProtectionUnavailableIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(requestId);
    Objects.requireNonNull(streakDate); Objects.requireNonNull(occurredAt);
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
  }
}
