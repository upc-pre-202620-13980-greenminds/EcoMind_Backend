package pe.greenminds.ecomind.iam.interfaces.rest.transform;

import pe.greenminds.ecomind.iam.domain.model.commands.ConfirmPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.SignInCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.SubmitRegistrationCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.VerifyEmailCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.ConfirmPasswordRecoveryResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.PasswordRecoveryResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.SignInResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.SubmitRegistrationResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.VerifyEmailResource;

/**
 * Converts the request resources of IAM into commands.
 */
public final class AuthenticationCommandFromResourceAssembler {

  private AuthenticationCommandFromResourceAssembler() {
  }

  public static SubmitRegistrationCommand toCommandFromResource(
      SubmitRegistrationResource resource) {
    return new SubmitRegistrationCommand(
        resource.name(),
        resource.email(),
        resource.password(),
        SocialRole.valueOf(resource.socialRole()));
  }

  public static VerifyEmailCommand toCommandFromResource(VerifyEmailResource resource) {
    return new VerifyEmailCommand(resource.email(), resource.code());
  }

  public static SignInCommand toCommandFromResource(SignInResource resource) {
    return new SignInCommand(resource.email(), resource.password());
  }

  public static RequestPasswordRecoveryCommand toCommandFromResource(
      PasswordRecoveryResource resource) {
    return new RequestPasswordRecoveryCommand(resource.email());
  }

  public static ConfirmPasswordRecoveryCommand toCommandFromResource(
      ConfirmPasswordRecoveryResource resource) {
    return new ConfirmPasswordRecoveryCommand(resource.token(), resource.newPassword());
  }
}
