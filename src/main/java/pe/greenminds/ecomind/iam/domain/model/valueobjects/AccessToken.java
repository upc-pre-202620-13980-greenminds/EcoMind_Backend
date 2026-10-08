package pe.greenminds.ecomind.iam.domain.model.valueobjects;

import java.time.Instant;

/**
 * Signed and temporary token that identifies an authenticated account.
 */
public record AccessToken(String value, Instant expiresAt) {

  public boolean isExpired(Instant now) {
    return !now.isBefore(expiresAt);
  }
}
