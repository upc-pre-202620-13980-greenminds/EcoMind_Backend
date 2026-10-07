package pe.greenminds.ecomind.quests.domain.model.queries;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;

public record GetQuestUsersByUserIdAndStatusQuery(
        Long userId,
        QuestStatus status
) {
}
