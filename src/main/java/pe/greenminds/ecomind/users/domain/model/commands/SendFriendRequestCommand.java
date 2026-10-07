package pe.greenminds.ecomind.users.domain.model.commands;

import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public record SendFriendRequestCommand(UserId requesterId, UserId receiverId) {
}
