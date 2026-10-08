package pe.greenminds.ecomind.quests.domain.model.events;

import java.time.OffsetDateTime;

public record CollaborativeQuestCompletedEvent(
        Long sessionId,
        Long questId,
        OffsetDateTime completedAt
) {
}
