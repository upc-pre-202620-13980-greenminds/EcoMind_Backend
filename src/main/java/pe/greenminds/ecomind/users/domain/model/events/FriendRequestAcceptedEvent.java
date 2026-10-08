package pe.greenminds.ecomind.users.domain.model.events;

import java.time.Instant;

public record FriendRequestAcceptedEvent(Long friendshipId, Instant occurredAt) {
}
