package pe.greenminds.ecomind.users.domain.model.commands;

import pe.greenminds.ecomind.users.domain.model.valueobjects.InvitationCode;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/** The receiver comes either as an id or as an invitation code; the other one is null. */
public record SendFriendRequestCommand(
    UserId requesterId, UserId receiverId, InvitationCode invitationCode) {

  public SendFriendRequestCommand(UserId requesterId, UserId receiverId) {
    this(requesterId, receiverId, null);
  }

  public SendFriendRequestCommand(UserId requesterId, InvitationCode invitationCode) {
    this(requesterId, null, invitationCode);
  }
}
