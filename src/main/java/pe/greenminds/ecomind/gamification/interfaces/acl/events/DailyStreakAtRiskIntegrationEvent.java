package pe.greenminds.ecomind.gamification.interfaces.acl.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record DailyStreakAtRiskIntegrationEvent(UUID eventId, UUID requestId, Long userId,
    LocalDate streakDate, Instant occurredAt) {
  public DailyStreakAtRiskIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(requestId);
    Objects.requireNonNull(streakDate); Objects.requireNonNull(occurredAt);
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
  }
}
