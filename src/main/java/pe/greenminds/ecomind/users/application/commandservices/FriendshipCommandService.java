package pe.greenminds.ecomind.users.application.commandservices;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.commands.RemoveFriendshipCommand;
import pe.greenminds.ecomind.users.domain.model.commands.RespondFriendRequestCommand;
import pe.greenminds.ecomind.users.domain.model.commands.SendFriendRequestCommand;

public interface FriendshipCommandService {

  Result<Friendship, ApplicationError> handle(SendFriendRequestCommand command);

  Result<Friendship, ApplicationError> handle(RespondFriendRequestCommand command);

  /** Returns the removed friendship; the controller answers 204 without a body. */
  Result<Friendship, ApplicationError> handle(RemoveFriendshipCommand command);
}
