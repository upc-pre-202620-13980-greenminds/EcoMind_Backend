package pe.greenminds.ecomind.users.domain.model.commands;

import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Accepts or rejects a friend request. Only the user who received it can respond.
 */
public record RespondFriendRequestCommand(UserId respondedBy, Long friendshipId, boolean accepted) {
}
