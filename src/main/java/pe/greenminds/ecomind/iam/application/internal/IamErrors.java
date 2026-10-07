package pe.greenminds.ecomind.iam.application.internal;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.ErrorType;

/**
 * Errors returned by the IAM application services. Each code has its translated message in
 * messages.properties under the key error.&lt;code in lowercase with hyphens&gt;.
 */
public final class IamErrors {

  private IamErrors() {
  }

  public static ApplicationError emailAlreadyRegistered() {
    return new ApplicationError(
        ErrorType.CONFLICT, "EMAIL_CONFLICT", "The email address is already registered", null);
  }

  public static ApplicationError passwordPolicyViolation() {
    return ApplicationError.validationError(
        "PASSWORD_POLICY_VIOLATION", "The password does not meet the security rules", null);
  }

  public static ApplicationError verificationCodeInvalid() {
    return ApplicationError.businessRuleViolation(
        "VERIFICATION_CODE_INVALID", "The verification code is not valid");
  }

  public static ApplicationError verificationCodeExpired() {
    return ApplicationError.businessRuleViolation(
        "VERIFICATION_CODE_EXPIRED", "The verification code has expired");
  }

  public static ApplicationError verificationAttemptsExceeded() {
    return ApplicationError.businessRuleViolation(
        "VERIFICATION_ATTEMPTS_EXCEEDED", "Too many verification attempts");
  }

  public static ApplicationError invalidCredentials() {
    return ApplicationError.unauthorized(
        "INVALID_CREDENTIALS", "The email or password is incorrect");
  }

  public static ApplicationError passwordResetTokenInvalid() {
    return ApplicationError.businessRuleViolation(
        "PASSWORD_RESET_TOKEN_INVALID", "The recovery link is not valid or has expired");
  }

  public static ApplicationError accountNotFound(Long accountId) {
    return ApplicationError.notFound("ACCOUNT", String.valueOf(accountId));
  }
}
