package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.function.Function;

public final class ResponseEntityAssembler {
    private ResponseEntityAssembler() {}

    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> mapper,
            HttpStatus successStatus
    ) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                    new ResponseEntity<>(mapper.apply(success.value()), successStatus);
            case Result.Failure<T, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
