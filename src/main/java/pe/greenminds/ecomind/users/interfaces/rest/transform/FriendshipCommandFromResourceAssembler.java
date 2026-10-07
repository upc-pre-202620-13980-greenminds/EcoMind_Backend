package pe.greenminds.ecomind.users.interfaces.rest.transform;

import pe.greenminds.ecomind.users.domain.model.commands.RespondFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.commands.SendFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.RespondFriendRequestResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.SendFriendRequestResource;

public final class FriendshipCommandFromResourceAssembler {

  private FriendshipCommandFromResourceAssembler() {
  }

  public static SendFriendRequestCommand toCommandFromResource(
      Long requesterId, SendFriendRequestResource resource) {
    return new SendFriendRequestCommand(
        new UserId(requesterId), new UserId(resource.receiverId()));
  }

  public static RespondFriendRequestCommand toCommandFromResource(
      Long respondedBy, Long friendshipId, RespondFriendRequestResource resource) {
    return new RespondFriendRequestCommand(
        new UserId(respondedBy), friendshipId, resource.accepted());
  }
}
