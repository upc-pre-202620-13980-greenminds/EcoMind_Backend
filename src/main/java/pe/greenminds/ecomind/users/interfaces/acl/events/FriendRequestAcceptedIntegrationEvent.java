package pe.greenminds.ecomind.users.interfaces.acl.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Public contract of Users for the other bounded contexts: a friend request was accepted. The
 * event id is the same for the same friendship, so a consumer can ignore a repeated delivery.
 */
public record FriendRequestAcceptedIntegrationEvent(
    UUID eventId, Long friendshipId, Long requesterId, Long receiverId, Instant occurredAt) {
}
