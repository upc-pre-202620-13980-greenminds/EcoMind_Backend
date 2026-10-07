package pe.greenminds.ecomind.iam.domain.model.commands;

/**
 * Verifies the email of a pending registration with the code sent to it and creates the account.
 */
public record VerifyEmailCommand(String email, String code) {
}
