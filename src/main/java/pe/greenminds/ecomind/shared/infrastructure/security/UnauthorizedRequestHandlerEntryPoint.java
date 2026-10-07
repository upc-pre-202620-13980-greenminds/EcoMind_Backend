package pe.greenminds.ecomind.shared.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Answers requests to protected routes that arrive without a valid token.
 * The exception is handed to the global exception handler so the 401 has the same localized body
 * as any other error.
 */
@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver handlerExceptionResolver;
  private final LocaleResolver localeResolver;

  public UnauthorizedRequestHandlerEntryPoint(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
      LocaleResolver localeResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
    this.localeResolver = localeResolver;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    // The security filters run before Spring MVC resolves the locale of the request.
    LocaleContextHolder.setLocale(localeResolver.resolveLocale(request));
    handlerExceptionResolver.resolveException(request, response, null, authException);
  }
}
