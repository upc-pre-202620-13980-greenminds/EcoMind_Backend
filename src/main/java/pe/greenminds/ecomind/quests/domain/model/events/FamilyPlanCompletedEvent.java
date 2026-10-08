package pe.greenminds.ecomind.quests.domain.model.events;

import java.time.OffsetDateTime;

public record FamilyPlanCompletedEvent(
        Long familyPlanId,
        Long familyId,
        Long ownerUserId,
        OffsetDateTime completedAt
) {
}
