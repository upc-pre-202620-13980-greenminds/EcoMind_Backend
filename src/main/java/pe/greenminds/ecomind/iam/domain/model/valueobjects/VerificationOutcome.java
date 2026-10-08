package pe.greenminds.ecomind.iam.domain.model.valueobjects;

/**
 * Result of checking a verification code against a pending registration.
 */
public enum VerificationOutcome {
  VERIFIED,
  INVALID_CODE,
  EXPIRED,
  ALREADY_USED,
  TOO_MANY_ATTEMPTS
}
