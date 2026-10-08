package pe.greenminds.ecomind.quests.domain.model.events;
import java.time.OffsetDateTime;
public record FamilyPlanActivatedEvent(Long familyPlanId, Long familyId, Long ownerUserId, OffsetDateTime occurredAt) {}
