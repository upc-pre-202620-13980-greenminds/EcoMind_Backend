package pe.greenminds.ecomind.quests.interfaces.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CollaborativeQuestCompletedIntegrationEvent(
        UUID eventId,
        Long sessionId,
        Long questId,
        Long versionGroupId,
        Integer versionNumber,
        List<Long> participantUserIds,
        Integer baseGems,
        Integer baseEcopoints,
        OffsetDateTime completedAt
) {
    public CollaborativeQuestCompletedIntegrationEvent {
        participantUserIds = List.copyOf(participantUserIds);
    }
}
