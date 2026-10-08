package pe.greenminds.ecomind.quests.interfaces.acl.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import pe.greenminds.ecomind.quests.interfaces.acl.resources.QuestRewardResource;

/** Contract only: the session identifies the shared execution; Quests validates every participant. */
public record CollaborativeQuestCompletedIntegrationEvent(UUID eventId, UUID sessionId, UUID questId,
    List<Long> participantIds, QuestRewardResource rewardPerParticipant, Instant occurredAt) {
  public CollaborativeQuestCompletedIntegrationEvent {
    Objects.requireNonNull(eventId); Objects.requireNonNull(sessionId); Objects.requireNonNull(questId);
    Objects.requireNonNull(rewardPerParticipant); Objects.requireNonNull(occurredAt);
    participantIds = List.copyOf(participantIds);
    if (participantIds.isEmpty() || participantIds.stream().anyMatch(id -> id <= 0)
        || participantIds.stream().distinct().count() != participantIds.size())
      throw new IllegalArgumentException("Unique positive participants are required");
  }
}
