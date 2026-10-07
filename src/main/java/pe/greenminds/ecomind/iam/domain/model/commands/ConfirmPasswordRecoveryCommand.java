package pe.greenminds.ecomind.iam.domain.model.commands;

/**
 * Consumes a recovery token and sets the new password of its account.
 */
public record ConfirmPasswordRecoveryCommand(String token, String newPassword) {
}
