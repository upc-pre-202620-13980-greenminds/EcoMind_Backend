package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.InviteCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.InviteCollabQuestMemberResource;

public final class InviteCollabQuestMemberCommandFromResourceAssembler {
    private InviteCollabQuestMemberCommandFromResourceAssembler() {
    }

    public static InviteCollabQuestMemberCommand toCommandFromResource(
            InviteCollabQuestMemberResource resource
    ) {
        return new InviteCollabQuestMemberCommand(
                resource.sessionId(),
                resource.invitedByUserId(),
                resource.invitedUserId()
        );
    }
}
