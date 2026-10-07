package pe.greenminds.ecomind.users.domain.model.events;

import java.time.Instant;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public record ProfileCreatedEvent(UserId userId, Instant occurredAt) {
}
