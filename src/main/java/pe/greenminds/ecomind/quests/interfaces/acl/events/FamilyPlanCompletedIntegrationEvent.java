package pe.greenminds.ecomind.quests.interfaces.acl.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** additionalEcopoints is the configured plan bonus, never the sum of its quests. */
public record FamilyPlanCompletedIntegrationEvent(UUID eventId, UUID executionId, UUID planId,
    Long familyId, List<Long> participantIds, long additionalEcopoints, Instant occurredAt) {
  public FamilyPlanCompletedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(executionId); Objects.requireNonNull(planId);
    Objects.requireNonNull(occurredAt);
    participantIds = List.copyOf(participantIds);
    if (familyId == null || familyId <= 0 || participantIds.isEmpty()
        || participantIds.stream().anyMatch(id -> id <= 0)
        || participantIds.stream().distinct().count() != participantIds.size())
      throw new IllegalArgumentException("Family and unique positive participants are required");
    if (additionalEcopoints < 0) throw new IllegalArgumentException("Family bonus cannot be negative");
  }
}
