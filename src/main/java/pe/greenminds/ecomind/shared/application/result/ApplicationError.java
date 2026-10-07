package pe.greenminds.ecomind.shared.application.result;

/**
 * Error produced by an application service.
 *
 * <p>The code is what the REST layer uses to pick the HTTP status and the localized message; the
 * message is only the English fallback used when no translation exists for the code.</p>
 *
 * @param code machine-readable error code, for example ACCOUNT_NOT_FOUND or VALIDATION_ERROR
 * @param message fallback message in English
 * @param details optional extra context, may be null
 */
public record ApplicationError(String code, String message, String details) {

  public ApplicationError(String code, String message) {
    this(code, message, null);
  }

  public static ApplicationError validationError(String fieldOrConcept, String reason) {
    return new ApplicationError(
        "VALIDATION_ERROR", "Validation failed: %s".formatted(fieldOrConcept), reason);
  }

  public static ApplicationError notFound(String resourceType, String identifier) {
    return new ApplicationError(
        "%s_NOT_FOUND".formatted(resourceType.toUpperCase()),
        "%s not found: %s".formatted(resourceType, identifier),
        null);
  }

  public static ApplicationError businessRuleViolation(String rule, String reason) {
    return new ApplicationError(
        "BUSINESS_RULE_VIOLATION", "Business rule violation: %s".formatted(rule), reason);
  }

  public static ApplicationError conflict(String resource, String reason) {
    return new ApplicationError(
        "%s_CONFLICT".formatted(resource.toUpperCase()),
        "Conflict with %s".formatted(resource),
        reason);
  }

  public static ApplicationError unexpected(String context, String reason) {
    return new ApplicationError(
        "UNEXPECTED_ERROR", "Unexpected error in %s".formatted(context), reason);
  }
}
