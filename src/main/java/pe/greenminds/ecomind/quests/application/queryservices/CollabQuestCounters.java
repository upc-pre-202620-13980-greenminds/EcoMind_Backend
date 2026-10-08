package pe.greenminds.ecomind.quests.application.queryservices;

public record CollabQuestCounters(
        int acceptedInvites,
        int pendingInvites,
        int activeInvites,
        int maxInvites
) {
}
