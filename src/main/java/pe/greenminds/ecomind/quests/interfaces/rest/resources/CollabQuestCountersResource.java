package pe.greenminds.ecomind.quests.interfaces.rest.resources;

public record CollabQuestCountersResource(
        int acceptedInvites,
        int pendingInvites,
        int activeInvites,
        int maxInvites
) {
}
