package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.QuestUserResource;

public final class QuestUserResourceFromEntityAssembler {
    private QuestUserResourceFromEntityAssembler() {
    }

    public static QuestUserResource toResourceFromEntity(QuestUser questUser) {
        return new QuestUserResource(
                questUser.getId(),
                questUser.getUserId(),
                questUser.getQuestId(),
                questUser.getStatus(),
                questUser.getProgress(),
                questUser.getEndDate(),
                questUser.getCollaborativeSessionId()
        );
    }
}
