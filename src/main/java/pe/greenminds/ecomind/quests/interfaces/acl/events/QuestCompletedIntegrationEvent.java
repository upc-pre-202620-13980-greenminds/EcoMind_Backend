package pe.greenminds.ecomind.quests.interfaces.acl.events;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuestCompletedIntegrationEvent(
        UUID eventId,
        Long questUserId,
        Long questId,
        Long versionGroupId,
        Integer versionNumber,
        Long userId,
        Category category,
        QuestType questType,
        Integer baseGems,
        Integer baseEcopoints,
        OffsetDateTime completedAt
) {
}
