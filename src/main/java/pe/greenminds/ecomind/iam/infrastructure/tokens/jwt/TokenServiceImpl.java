package pe.greenminds.ecomind.iam.infrastructure.tokens.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccessToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;

/**
 * Issues JWT access tokens signed with HMAC. The subject is the account id, which is all the
 * other bounded contexts need to identify the user.
 */
@Service
public class TokenServiceImpl implements TokenService {

  private final SecretKey signingKey;
  private final Duration validity;

  public TokenServiceImpl(
      @Value("${authorization.jwt.secret}") String secret,
      @Value("${authorization.jwt.expiration-minutes}") long expirationMinutes) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.validity = Duration.ofMinutes(expirationMinutes);
  }

  @Override
  public AccessToken issueAccessToken(AuthenticatedUser authenticatedUser) {
    // JWT dates have second precision.
    Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant expiresAt = issuedAt.plus(validity);
    String token =
        Jwts.builder()
            .subject(String.valueOf(authenticatedUser.accountId().value()))
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact();
    return new AccessToken(token, expiresAt);
  }
}
