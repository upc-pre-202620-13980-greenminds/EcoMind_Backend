package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestMemberResource;

public final class CollabQuestMemberResourceFromEntityAssembler {
    private CollabQuestMemberResourceFromEntityAssembler() {
    }

    public static CollabQuestMemberResource toResourceFromEntity(
            CollabQuestMember collabQuestMember
    ) {
        return new CollabQuestMemberResource(
                collabQuestMember.getId(),
                collabQuestMember.getSessionId(),
                collabQuestMember.getUserId(),
                collabQuestMember.getOwnerId(),
                collabQuestMember.getRole(),
                collabQuestMember.getStatus(),
                collabQuestMember.getAnswerDate(),
                collabQuestMember.getRevokeDate()
        );
    }
}
