package pe.greenminds.ecomind.quests.domain.model.commands;

public record InviteCollabQuestMemberCommand(
        Long sessionId,
        Long invitedByUserId,
        Long invitedUserId
) {
}
