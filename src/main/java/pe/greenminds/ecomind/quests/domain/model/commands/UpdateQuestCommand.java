package pe.greenminds.ecomind.quests.domain.model.commands;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import java.time.LocalDate;

public record UpdateQuestCommand(
        Long questId,
        Long minigameId,
        String title,
        String description,
        Category category,
        QuestType type,
        Integer gemReward,
        Integer ecopoints,
        Integer age,
        Integer time,
        Theme theme,
        LocalDate assignedDate,
        String image
) {
}
