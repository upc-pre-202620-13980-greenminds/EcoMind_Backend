package pe.greenminds.ecomind.users.interfaces.acl;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.application.commandservices.ProfileCommandService;
import pe.greenminds.ecomind.users.domain.model.commands.CreateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Entry point of Users for the IAM bounded context. It only receives simple values, so IAM never
 * depends on the Users model, and translates them into the CreateProfile command.
 */
@Component
public class IamContextListener {

  private final ProfileCommandService profileCommandService;

  public IamContextListener(ProfileCommandService profileCommandService) {
    this.profileCommandService = profileCommandService;
  }

  /**
   * Creates the profile of a new account.
   *
   * @throws IllegalStateException when the profile cannot be created, so the caller can undo the
   *     creation of the account
   */
  public void createProfile(Long accountId, String name, String socialRole) {
    var command =
        new CreateProfileCommand(new UserId(accountId), name, SocialRole.valueOf(socialRole));
    if (profileCommandService.handle(command)
        instanceof Result.Failure<?, ApplicationError> failure) {
      throw new IllegalStateException(
          "Profile could not be created: " + failure.error().code());
    }
  }
}
