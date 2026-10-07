package pe.greenminds.ecomind.quests.application.queryservices;

public record CollabQuestPermissions(
        boolean canInvite,
        boolean canStart,
        boolean canAcceptInvitation,
        boolean canLeave,
        boolean canRemoveMembers,
        boolean canDeleteSession
) {
}
