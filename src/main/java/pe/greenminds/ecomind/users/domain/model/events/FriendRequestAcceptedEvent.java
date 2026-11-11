package pe.greenminds.ecomind.users.domain.model.events;

import java.time.Instant;

/** Published when the receiver accepts a friend request; Notifications tells the requester. */
public record FriendRequestAcceptedEvent(
    Long friendshipId, Long requesterId, Long receiverId, Instant occurredAt) {
}
