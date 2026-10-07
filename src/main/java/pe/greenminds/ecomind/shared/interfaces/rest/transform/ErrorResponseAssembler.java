package pe.greenminds.ecomind.shared.interfaces.rest.transform;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.ErrorType;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;

/**
 * Builds error responses with the message translated to the locale of the request.
 */
@Component
public class ErrorResponseAssembler {

  private final MessageSource messageSource;

  public ErrorResponseAssembler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Maps an application error to its HTTP response. The message is looked up first with a key
   * specific to the code (ACCOUNT_NOT_FOUND -> error.account-not-found), then with the generic key
   * of its type, and finally falls back to the message carried by the error.
   */
  public ResponseEntity<ErrorResource> toErrorResponseFromApplicationError(ApplicationError error) {
    Locale locale = LocaleContextHolder.getLocale();
    Object[] args = {toResourceNameFromErrorCode(error.code())};
    String genericMessage =
        messageSource.getMessage(
            toGenericMessageKeyFromErrorType(error.type()), args, error.message(), locale);
    String message =
        messageSource.getMessage(
            toSpecificMessageKeyFromErrorCode(error.code()), args, genericMessage, locale);
    return toErrorResponse(
        toStatusFromErrorType(error.type()), error.code(), message, error.details());
  }

  /** Builds an error response from a message key, used for errors raised outside the services. */
  public ResponseEntity<ErrorResource> toErrorResponseFromMessageKey(
      HttpStatusCode status, String code, String messageKey, String details, Object... args) {
    String message =
        messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
    return toErrorResponse(status, code, message, details);
  }

  public static HttpStatusCode toStatusFromErrorType(ErrorType type) {
    return switch (type) {
      case VALIDATION -> HttpStatus.BAD_REQUEST;
      case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case CONFLICT -> HttpStatus.CONFLICT;
      case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_CONTENT;
      case UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  private static ResponseEntity<ErrorResource> toErrorResponse(
      HttpStatusCode status, String code, String message, String details) {
    return ResponseEntity.status(status).body(new ErrorResource(code, message, details));
  }

  private static String toSpecificMessageKeyFromErrorCode(String errorCode) {
    return "error." + errorCode.toLowerCase(Locale.ROOT).replace('_', '-');
  }

  private static String toGenericMessageKeyFromErrorType(ErrorType type) {
    return switch (type) {
      case VALIDATION -> "error.validation";
      case UNAUTHORIZED -> "error.unauthorized";
      case NOT_FOUND -> "error.not-found";
      case CONFLICT -> "error.conflict";
      case BUSINESS_RULE -> "error.business-rule";
      case UNEXPECTED -> "error.unexpected";
    };
  }

  private static String toResourceNameFromErrorCode(String errorCode) {
    return errorCode
        .replace("_NOT_FOUND", "")
        .replace("_CONFLICT", "")
        .toLowerCase(Locale.ROOT)
        .replace('_', ' ');
  }
}
