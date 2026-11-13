package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestInvitation;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestInvitationResource;

public final class CollabQuestInvitationResourceAssembler {
    private CollabQuestInvitationResourceAssembler() {}

    public static CollabQuestInvitationResource toResource(CollabQuestInvitation invitation) {
        return new CollabQuestInvitationResource(invitation.memberId(), invitation.sessionId(),
                invitation.questId(), invitation.questTitle(), invitation.organizerUserId(),
                invitation.invitedUserId(), invitation.memberStatus(), invitation.sessionStatus());
    }
}
