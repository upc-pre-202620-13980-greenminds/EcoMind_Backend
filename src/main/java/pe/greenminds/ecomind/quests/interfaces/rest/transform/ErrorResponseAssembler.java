package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;

public final class ErrorResponseAssembler {
    private ErrorResponseAssembler() {}

    public static ResponseEntity<?> toErrorResponseFromApplicationError(ApplicationError error) {
        HttpStatus status = HttpStatus.valueOf(
                pe.greenminds.ecomind.shared.interfaces.rest.transform.ErrorResponseAssembler
                        .toStatusFromErrorType(error.type()).value()
        );
        return new ResponseEntity<>(error, status);
    }
}
