package pe.greenminds.ecomind.iam.domain.model.aggregates;

import java.time.Duration;
import java.time.Instant;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;

/**
 * Temporary, single-use authorization to change the password of an account.
 */
public class PasswordResetToken {

  public static final Duration VALIDITY = Duration.ofMinutes(30);

  private final Long id;
  private final AccountId accountId;
  private final TokenHash tokenHash;
  private final Instant expiresAt;
  private Instant usedAt;
  private TokenHash confirmationIdHash;

  /** Rebuilds a token that already exists; the id is null until persisted. */
  public PasswordResetToken(
      Long id, AccountId accountId, TokenHash tokenHash, Instant expiresAt, Instant usedAt) {
    this(id, accountId, tokenHash, expiresAt, usedAt, null);
  }

  public PasswordResetToken(
      Long id, AccountId accountId, TokenHash tokenHash, Instant expiresAt,
      Instant usedAt, TokenHash confirmationIdHash) {
    this.confirmationIdHash = confirmationIdHash;
    this.id = id;
    this.accountId = accountId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
  }

  public static PasswordResetToken issue(AccountId accountId, TokenHash tokenHash, Instant now) {
    return new PasswordResetToken(null, accountId, tokenHash, now.plus(VALIDITY), null);
  }

  public boolean isValid(Instant now) {
    return usedAt == null && now.isBefore(expiresAt);
  }

  /** Marks the token as used. Returns false when it was already used or has expired. */
  public boolean consume(Instant now) {
    return consume(now, null);
  }

  public boolean consume(Instant now, TokenHash confirmationIdHash) {
    if (!isValid(now)) {
      return false;
    }
    usedAt = now;
    this.confirmationIdHash = confirmationIdHash;
    return true;
  }

  /** A retry acknowledges the original change; it never consumes the token again. */
  public boolean isConfirmationRetry(TokenHash confirmationIdHash, Instant now) {
    return usedAt != null && this.confirmationIdHash != null
        && this.confirmationIdHash.equals(confirmationIdHash)
        && now.isBefore(usedAt.plus(Duration.ofMinutes(5)));
  }

  public TokenHash getConfirmationIdHash() {
    return confirmationIdHash;
  }

  public Long getId() {
    return id;
  }

  public AccountId getAccountId() {
    return accountId;
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
}
