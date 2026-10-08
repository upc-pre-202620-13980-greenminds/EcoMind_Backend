package pe.greenminds.ecomind.quests.interfaces.acl.events;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public record FamilyPlanActivatedIntegrationEvent(UUID eventId, Long familyPlanId, Long familyId, Long ownerUserId, List<Long> participantUserIds, OffsetDateTime occurredAt) {
    public FamilyPlanActivatedIntegrationEvent { participantUserIds = List.copyOf(participantUserIds); }
}
