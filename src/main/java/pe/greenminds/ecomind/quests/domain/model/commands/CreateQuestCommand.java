package pe.greenminds.ecomind.quests.domain.model.commands;


import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import java.time.LocalDate;

public record CreateQuestCommand (
        Long minimageId,
        Category category,
        String title,
        String description,
        String image,
        QuestType type,
        Integer reward_gems,
        Integer reward_ecopoints,
        Integer time,
        Theme theme,
        Integer age,
        LocalDate assignedDate
) {
}
