package pe.greenminds.ecomind.shared.application.result;

/**
 * Category of an application error. The REST layer maps each category to an HTTP status.
 */
public enum ErrorType {
  VALIDATION,
  UNAUTHORIZED,
  NOT_FOUND,
  CONFLICT,
  BUSINESS_RULE,
  UNEXPECTED
}
