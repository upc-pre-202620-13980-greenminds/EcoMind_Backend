package pe.greenminds.ecomind.notifications.domain.model.commands;

import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

/** Sent by the consumer of the Users event when a friend request is accepted. */
public record NotifyFriendRequestAcceptedCommand(RecipientId requesterId, String accepterName) {
}
