package pe.greenminds.ecomind.shared.application.result;

/**
 * Error produced by an application service.
 *
 * <p>The type decides the HTTP status and the code selects the localized message
 * (ACCOUNT_NOT_FOUND -> error.account-not-found). The message is only the English fallback used
 * when no translation exists for the code.</p>
 *
 * @param type category of the error
 * @param code machine-readable error code, for example ACCOUNT_NOT_FOUND or VALIDATION_ERROR
 * @param message fallback message in English
 * @param details optional extra context, may be null
 */
public record ApplicationError(ErrorType type, String code, String message, String details) {

  public static ApplicationError validationError(String fieldOrConcept, String reason) {
    return new ApplicationError(
        ErrorType.VALIDATION,
        "VALIDATION_ERROR",
        "Validation failed: %s".formatted(fieldOrConcept),
        reason);
  }

  /** Validation error with its own code, so it can have a specific translated message. */
  public static ApplicationError validationError(String code, String message, String details) {
    return new ApplicationError(ErrorType.VALIDATION, code, message, details);
  }

  public static ApplicationError unauthorized(String code, String message) {
    return new ApplicationError(ErrorType.UNAUTHORIZED, code, message, null);
  }

  public static ApplicationError notFound(String resourceType, String identifier) {
    return new ApplicationError(
        ErrorType.NOT_FOUND,
        "%s_NOT_FOUND".formatted(resourceType.toUpperCase()),
        "%s not found: %s".formatted(resourceType, identifier),
        null);
  }

  public static ApplicationError conflict(String resource, String reason) {
    return new ApplicationError(
        ErrorType.CONFLICT,
        "%s_CONFLICT".formatted(resource.toUpperCase()),
        "Conflict with %s".formatted(resource),
        reason);
  }

  /** Business rule violation with its own code, so it can have a specific translated message. */
  public static ApplicationError businessRuleViolation(String code, String message) {
    return new ApplicationError(ErrorType.BUSINESS_RULE, code, message, null);
  }

  public static ApplicationError unexpected(String context, String reason) {
    return new ApplicationError(
        ErrorType.UNEXPECTED,
        "UNEXPECTED_ERROR",
        "Unexpected error in %s".formatted(context),
        reason);
  }
}
