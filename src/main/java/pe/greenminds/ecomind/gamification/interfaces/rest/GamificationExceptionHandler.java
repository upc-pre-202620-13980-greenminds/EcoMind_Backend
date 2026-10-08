package pe.greenminds.ecomind.gamification.interfaces.rest;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationDependencyUnavailableException;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ErrorResponseAssembler;

@RestControllerAdvice(basePackages = "pe.greenminds.ecomind.gamification.interfaces.rest")
@Order(0)
public class GamificationExceptionHandler {
    private final ErrorResponseAssembler errors;

    public GamificationExceptionHandler(ErrorResponseAssembler errors) {
        this.errors = errors;
    }

    @ExceptionHandler(GamificationDependencyUnavailableException.class)
    public ResponseEntity<ErrorResource> unavailable(
            GamificationDependencyUnavailableException failure) {
        return errors.toErrorResponseFromMessageKey(
                HttpStatus.SERVICE_UNAVAILABLE,
                "GAMIFICATION_DEPENDENCY_UNAVAILABLE",
                "error.gamification-dependency-unavailable",
                null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResource> forbidden(AccessDeniedException failure) {
        return errors.toErrorResponseFromApplicationError(
                ApplicationError.forbidden(
                        "COMMUNITY_ACCESS_FORBIDDEN", "Community membership is required"));
    }
}
