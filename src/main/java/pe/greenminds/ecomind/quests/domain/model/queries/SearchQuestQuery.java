package pe.greenminds.ecomind.quests.domain.model.queries;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

public record SearchQuestQuery(
        String title,
        Category category,
        QuestType questType,
        Integer age,
        Theme type
) {
}