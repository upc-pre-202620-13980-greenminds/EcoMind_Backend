package pe.greenminds.ecomind.iam.domain.model.entities;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;

/**
 * Credential of an account: its normalized email and the hash of its password.
 */
public class AccountCredential {

  private final EmailAddress email;
  private PasswordHash passwordHash;

  public AccountCredential(EmailAddress email, PasswordHash passwordHash) {
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public boolean matchesPassword(String rawPassword, PasswordHasher passwordHasher) {
    return passwordHasher.matches(rawPassword, passwordHash);
  }

  public void updatePassword(PasswordHash newPasswordHash) {
    this.passwordHash = newPasswordHash;
  }

  public EmailAddress getEmail() {
    return email;
  }

  public PasswordHash getPasswordHash() {
    return passwordHash;
  }
}
