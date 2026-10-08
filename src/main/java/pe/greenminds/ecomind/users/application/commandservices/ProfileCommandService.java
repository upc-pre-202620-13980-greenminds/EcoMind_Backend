package pe.greenminds.ecomind.users.application.commandservices;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.commands.CreateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.commands.UpdateProfileCommand;

public interface ProfileCommandService {

  Result<UserProfile, ApplicationError> handle(CreateProfileCommand command);

  Result<UserProfile, ApplicationError> handle(UpdateProfileCommand command);
}
