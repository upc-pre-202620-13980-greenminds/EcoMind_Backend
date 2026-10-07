package pe.greenminds.ecomind.quests.interfaces.acl.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import pe.greenminds.ecomind.quests.interfaces.acl.resources.QuestRewardResource;

/** Published only after Quests validates completion. executionId is stable across message retries. */
public record QuestCompletedIntegrationEvent(UUID eventId, UUID executionId, UUID questId,
    Long userId, String category, Instant occurredAt, LocalDate activityDate,
    boolean countsForDailyStreak, QuestRewardResource baseReward) {
  public QuestCompletedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(executionId); Objects.requireNonNull(questId);
    Objects.requireNonNull(occurredAt); Objects.requireNonNull(activityDate); Objects.requireNonNull(baseReward);
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    if (category == null || category.isBlank()) throw new IllegalArgumentException("Quest category is required");
  }
}
