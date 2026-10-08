package pe.greenminds.ecomind.iam.domain.model.entities;

import java.time.Instant;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.VerificationOutcome;

/**
 * Verification code of a pending registration. It can be used once, within its validity period
 * and with a limited number of attempts.
 */
public class EmailVerification {

  public static final int MAX_ATTEMPTS = 5;

  private final TokenHash tokenHash;
  private final Instant expiresAt;
  private Instant usedAt;
  private int attempts;

  public EmailVerification(TokenHash tokenHash, Instant expiresAt, Instant usedAt, int attempts) {
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.attempts = attempts;
  }

  /** Checks the code and records the attempt. Only a correct code marks it as used. */
  public VerificationOutcome verify(TokenHash candidate, Instant now) {
    if (usedAt != null) {
      return VerificationOutcome.ALREADY_USED;
    }
    if (!now.isBefore(expiresAt)) {
      return VerificationOutcome.EXPIRED;
    }
    if (attempts >= MAX_ATTEMPTS) {
      return VerificationOutcome.TOO_MANY_ATTEMPTS;
    }
    attempts++;
    if (!tokenHash.equals(candidate)) {
      return VerificationOutcome.INVALID_CODE;
    }
    usedAt = now;
    return VerificationOutcome.VERIFIED;
  }

  public boolean isValid(Instant now) {
    return usedAt == null && now.isBefore(expiresAt) && attempts < MAX_ATTEMPTS;
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public TokenHash getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public int getAttempts() {
    return attempts;
  }
}
