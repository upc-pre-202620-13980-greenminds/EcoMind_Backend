package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.QuestResource;

public class QuestResourceFromEntityAssembler {

    private QuestResourceFromEntityAssembler() {}

    public static QuestResource toResourceFromEntity(Quest quest) {
        var reward = quest.getReward();

        return new QuestResource(
                quest.getId(),
                quest.getMinigameId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getCategory(),
                quest.getType(),
                reward.gems(),
                reward.ecopoints(),
                quest.getAge(),
                quest.getTime(),
                quest.getTheme(),
                quest.getAssignedDate(),
                quest.getImage()
        );
    }
}
