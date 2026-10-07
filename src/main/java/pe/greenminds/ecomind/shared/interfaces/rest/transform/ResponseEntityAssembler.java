package pe.greenminds.ecomind.shared.interfaces.rest.transform;

import java.util.function.Function;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

/**
 * Translates the Result of an application service into an HTTP response.
 */
@Component
public class ResponseEntityAssembler {

  private final ErrorResponseAssembler errorResponseAssembler;

  public ResponseEntityAssembler(ErrorResponseAssembler errorResponseAssembler) {
    this.errorResponseAssembler = errorResponseAssembler;
  }

  /**
   * Returns the success value converted to a resource with the given status, or the localized
   * error response when the result is a failure.
   */
  public <T, R> ResponseEntity<?> toResponseEntityFromResult(
      Result<T, ApplicationError> result,
      Function<T, R> successResourceAssembler,
      HttpStatusCode successStatus) {
    return switch (result) {
      case Result.Success<T, ApplicationError> success ->
          new ResponseEntity<>(successResourceAssembler.apply(success.value()), successStatus);
      case Result.Failure<T, ApplicationError> failure ->
          errorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
    };
  }
}
