package pe.greenminds.ecomind.shared.interfaces.rest;

import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ErrorResponseAssembler;

/**
 * Converts the exceptions that escape the controllers into localized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final ErrorResponseAssembler errorResponseAssembler;
  private final MessageSource messageSource;

  public GlobalExceptionHandler(
      ErrorResponseAssembler errorResponseAssembler, MessageSource messageSource) {
    this.errorResponseAssembler = errorResponseAssembler;
    this.messageSource = messageSource;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResource> handleValidation(MethodArgumentNotValidException ex) {
    Locale locale = LocaleContextHolder.getLocale();
    String details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + messageSource.getMessage(error, locale))
            .collect(Collectors.joining("; "));
    return errorResponseAssembler.toErrorResponseFromMessageKey(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "error.validation", details);
  }

  @ExceptionHandler({
      HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class,
      IllegalArgumentException.class
  })
  public ResponseEntity<ErrorResource> handleBadRequest(Exception ex) {
    return errorResponseAssembler.toErrorResponseFromMessageKey(
        HttpStatus.BAD_REQUEST, "BAD_REQUEST", "error.bad-request", null);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResource> handleNoResourceFound(NoResourceFoundException ex) {
    return errorResponseAssembler.toErrorResponseFromMessageKey(
        HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND", "error.endpoint-not-found", null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResource> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    return errorResponseAssembler.toErrorResponseFromMessageKey(
        HttpStatus.METHOD_NOT_ALLOWED,
        "METHOD_NOT_ALLOWED",
        "error.method-not-allowed",
        null,
        ex.getMethod());
  }

  /**
   * Last resort. Client errors raised by Spring MVC keep their status; anything else is logged and
   * answered with a generic message so internal details never reach the client.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResource> handleUnexpected(Exception ex) {
    if (ex instanceof ErrorResponse errorResponse
        && errorResponse.getStatusCode().is4xxClientError()) {
      return errorResponseAssembler.toErrorResponseFromMessageKey(
          errorResponse.getStatusCode(), "BAD_REQUEST", "error.bad-request", null);
    }
    LOGGER.error("Unhandled exception", ex);
    return errorResponseAssembler.toErrorResponseFromMessageKey(
        HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "error.unexpected", null);
  }
}
