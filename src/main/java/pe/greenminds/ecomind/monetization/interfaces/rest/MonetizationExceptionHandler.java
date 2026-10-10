package pe.greenminds.ecomind.monetization.interfaces.rest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.InsufficientGemBalanceException;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ErrorResponseAssembler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = StoreController.class)
public class MonetizationExceptionHandler {
  private final ErrorResponseAssembler errors;

  public MonetizationExceptionHandler(ErrorResponseAssembler errors) {
    this.errors = errors;
  }

  @ExceptionHandler(InsufficientGemBalanceException.class)
  public ResponseEntity<ErrorResource> insufficientBalance(
      InsufficientGemBalanceException exception) {
    return errors.toErrorResponseFromMessageKey(
        HttpStatus.CONFLICT, "INSUFFICIENT_GEM_BALANCE", "error.insufficient-gem-balance", null);
  }
}
