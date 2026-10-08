package pe.greenminds.ecomind.quests.interfaces.acl.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MinigameCompletedIntegrationEvent(
        UUID eventId,
        Long attemptId,
        Long questId,
        Long versionGroupId,
        Integer versionNumber,
        Long minigameId,
        Long userId,
        Integer score,
        Integer baseGems,
        Integer baseEcopoints,
        OffsetDateTime completedAt
) {
}
