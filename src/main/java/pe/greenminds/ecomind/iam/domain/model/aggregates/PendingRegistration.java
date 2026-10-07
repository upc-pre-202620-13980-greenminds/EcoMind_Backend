package pe.greenminds.ecomind.iam.domain.model.aggregates;

import java.time.Duration;
import java.time.Instant;
import pe.greenminds.ecomind.iam.domain.model.entities.EmailVerification;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.VerificationOutcome;

/**
 * Registration data kept until the email is verified. The account does not exist before that.
 */
public class PendingRegistration {

  public static final Duration VALIDITY = Duration.ofMinutes(20);

  private final Long id;
  private final String name;
  private final EmailAddress email;
  private final PasswordHash passwordHash;
  private final SocialRole socialRole;
  private final Instant expiresAt;
  private final Instant createdAt;
  private final EmailVerification emailVerification;

  /** Rebuilds a pending registration that already exists; the id is null until persisted. */
  public PendingRegistration(
      Long id,
      String name,
      EmailAddress email,
      PasswordHash passwordHash,
      SocialRole socialRole,
      Instant expiresAt,
      Instant createdAt,
      EmailVerification emailVerification) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
    this.socialRole = socialRole;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.emailVerification = emailVerification;
  }

  public static PendingRegistration create(
      String name,
      EmailAddress email,
      PasswordHash passwordHash,
      SocialRole socialRole,
      TokenHash verificationCodeHash,
      Instant now) {
    Instant expiresAt = now.plus(VALIDITY);
    return new PendingRegistration(
        null,
        name.trim(),
        email,
        passwordHash,
        socialRole,
        expiresAt,
        now,
        new EmailVerification(verificationCodeHash, expiresAt, null, 0));
  }

  public VerificationOutcome verifyEmail(TokenHash verificationCodeHash, Instant now) {
    if (isExpired(now) && !emailVerification.isUsed()) {
      return VerificationOutcome.EXPIRED;
    }
    return emailVerification.verify(verificationCodeHash, now);
  }

  public boolean isExpired(Instant now) {
    return !now.isBefore(expiresAt);
  }

  public boolean isVerified() {
    return emailVerification.isUsed();
  }

  /** Creates the account of this registration. Only allowed once the email is verified. */
  public Account createAccount() {
    if (!isVerified()) {
      throw new IllegalStateException("The email must be verified before creating the account");
    }
    Account account = Account.create(email, passwordHash);
    account.activate();
    return account;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public EmailAddress getEmail() {
    return email;
  }

  public PasswordHash getPasswordHash() {
    return passwordHash;
  }

  public SocialRole getSocialRole() {
    return socialRole;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public EmailVerification getEmailVerification() {
    return emailVerification;
  }
}
