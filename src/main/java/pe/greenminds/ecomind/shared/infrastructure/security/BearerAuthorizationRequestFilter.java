package pe.greenminds.ecomind.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates the request when it carries a valid bearer token.
 * Requests without a valid token continue unauthenticated and are rejected later if the route is
 * protected.
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AccessTokenVerifier accessTokenVerifier;

  public BearerAuthorizationRequestFilter(AccessTokenVerifier accessTokenVerifier) {
    this.accessTokenVerifier = accessTokenVerifier;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      accessTokenVerifier
          .verify(header.substring(BEARER_PREFIX.length()))
          .ifPresent(BearerAuthorizationRequestFilter::authenticate);
    }
    filterChain.doFilter(request, response);
  }

  private static void authenticate(Long accountId) {
    var authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            new AuthenticatedUserPrincipal(accountId), null, List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
