package pe.greenminds.ecomind.quests.interfaces.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FamilyPlanCompletedIntegrationEvent(
        UUID eventId,
        Long familyPlanId,
        Long familyId,
        Long ownerUserId,
        List<Long> participantUserIds,
        OffsetDateTime completedAt
) {
    public FamilyPlanCompletedIntegrationEvent {
        participantUserIds = List.copyOf(participantUserIds);
    }
}
