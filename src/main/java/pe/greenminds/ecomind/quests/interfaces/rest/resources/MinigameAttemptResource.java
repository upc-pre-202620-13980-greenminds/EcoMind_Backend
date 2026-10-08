package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;

import java.time.OffsetDateTime;
import java.util.Map;

public record MinigameAttemptResource(
        Long id,
        Long userId,
        Long questId,
        Integer score,
        MinigameAttemptStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        Map<String, Object> metadata,
        Integer givenGems,
        Integer givenEcopoints
) {
}
