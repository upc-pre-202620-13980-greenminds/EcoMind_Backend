package pe.greenminds.ecomind.quests.domain.model.events;

import java.time.OffsetDateTime;

public record MinigameCompletedEvent(
        Long attemptId,
        Long questId,
        Long minigameId,
        Long userId,
        Integer score,
        OffsetDateTime completedAt
) {
}
