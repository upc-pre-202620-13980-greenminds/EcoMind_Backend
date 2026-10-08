package pe.greenminds.ecomind.quests.domain.model.events;

import java.time.OffsetDateTime;

public record QuestCompletedEvent(
        Long questUserId,
        Long questId,
        Long userId,
        Long collaborativeSessionId,
        OffsetDateTime completedAt
) {
}
