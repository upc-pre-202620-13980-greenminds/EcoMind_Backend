package pe.greenminds.ecomind.iam.interfaces.rest.transform;

import pe.greenminds.ecomind.iam.application.commandservices.SignInResult;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.AuthenticationResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.PendingRegistrationResource;

/**
 * Converts the results of IAM into response resources. Hashes, codes and recovery tokens are never
 * part of a response.
 */
public final class AuthenticationResourceAssembler {

  private AuthenticationResourceAssembler() {
  }

  public static PendingRegistrationResource toResourceFromEntity(
      PendingRegistration pendingRegistration) {
    return new PendingRegistrationResource(
        pendingRegistration.getEmail().value(), pendingRegistration.getExpiresAt());
  }

  public static AuthenticatedUserResource toResourceFromEntity(Account account) {
    return new AuthenticatedUserResource(account.getId().value(), account.getEmail().value());
  }

  public static AuthenticatedUserResource toResourceFromEntity(AuthenticatedUser user) {
    return new AuthenticatedUserResource(user.accountId().value(), user.email().value());
  }

  public static AuthenticationResource toResourceFromEntity(SignInResult signInResult) {
    return new AuthenticationResource(
        signInResult.accessToken().value(),
        signInResult.accessToken().expiresAt(),
        signInResult.authenticatedUser().accountId().value(),
        signInResult.authenticatedUser().email().value());
  }
}
