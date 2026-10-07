package pe.greenminds.ecomind.users.domain.model.events;

import java.time.Instant;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public record FamilyMemberAddedEvent(FamilyId familyId, UserId memberUserId, Instant occurredAt) {
}
