package pe.greenminds.ecomind.iam.domain.model.valueobjects;

/**
 * Identity of an account whose credentials were verified.
 */
public record AuthenticatedUser(AccountId accountId, EmailAddress email) {
}
