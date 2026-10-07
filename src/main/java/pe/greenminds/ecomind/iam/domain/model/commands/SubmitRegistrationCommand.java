package pe.greenminds.ecomind.iam.domain.model.commands;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;

/**
 * Starts a pending registration and requests the verification email.
 */
public record SubmitRegistrationCommand(
    String name, String email, String password, SocialRole socialRole) {
}
