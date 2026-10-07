package pe.greenminds.ecomind.quests.interfaces.acl.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Contract only: attemptId is the canonical execution used for deduplication and repeat history. */
public record MinigameCompletedIntegrationEvent(UUID eventId, UUID attemptId, UUID questId,
    Long userId, long score, Instant occurredAt) {
  public MinigameCompletedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(attemptId); Objects.requireNonNull(questId);
    Objects.requireNonNull(occurredAt);
    if (userId == null || userId <= 0 || score < 0) throw new IllegalArgumentException("Invalid minigame result");
  }
}
