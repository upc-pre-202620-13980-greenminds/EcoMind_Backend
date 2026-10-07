package pe.greenminds.ecomind.shared.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validates the signature and expiration of an access token without calling any bounded context.
 */
@Component
public class AccessTokenVerifier {

  private final SecretKey signingKey;

  public AccessTokenVerifier(@Value("${authorization.jwt.secret}") String secret) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /** Returns the account id stored as subject, or empty when the token is not valid. */
  public Optional<Long> verify(String token) {
    try {
      String subject =
          Jwts.parser()
              .verifyWith(signingKey)
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .getSubject();
      return Optional.of(Long.valueOf(subject));
    } catch (JwtException | IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
