package pe.greenminds.ecomind.iam.application.commandservices;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccessToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;

/**
 * Outcome of a successful sign-in: who signed in and the access token issued.
 */
public record SignInResult(AuthenticatedUser authenticatedUser, AccessToken accessToken) {
}
