package pe.greenminds.ecomind.gamification.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Contract for durable delivery to Monetization; event and reward IDs remain stable on retry. */
public record RewardGrantedIntegrationEvent(UUID eventId, UUID rewardId, Long userId, int gems, Instant occurredAt) {
  public RewardGrantedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(rewardId); Objects.requireNonNull(occurredAt);
    if (userId == null || userId <= 0 || gems <= 0) throw new IllegalArgumentException("Invalid gem grant");
  }
}
