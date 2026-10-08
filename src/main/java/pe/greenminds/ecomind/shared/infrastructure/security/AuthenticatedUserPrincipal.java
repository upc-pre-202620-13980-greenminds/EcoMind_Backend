package pe.greenminds.ecomind.shared.infrastructure.security;

/**
 * Identity of the request once its access token has been validated.
 *
 * @param accountId id of the authenticated account, taken from the subject of the token
 */
public record AuthenticatedUserPrincipal(Long accountId) {
}
