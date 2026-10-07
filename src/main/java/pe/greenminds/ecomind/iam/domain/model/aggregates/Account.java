package pe.greenminds.ecomind.iam.domain.model.aggregates;

import pe.greenminds.ecomind.iam.domain.model.entities.AccountCredential;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountStatus;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;

/**
 * Digital identity that can sign in to EcoMind. It owns its credential.
 */
public class Account {

  private final AccountId id;
  private AccountStatus status;
  private final AccountCredential credential;

  /** Rebuilds an account that already exists; the id is null until it is persisted. */
  public Account(AccountId id, AccountStatus status, AccountCredential credential) {
    this.id = id;
    this.status = status;
    this.credential = credential;
  }

  public static Account create(EmailAddress email, PasswordHash passwordHash) {
    return new Account(null, AccountStatus.INACTIVE, new AccountCredential(email, passwordHash));
  }

  public void activate() {
    this.status = AccountStatus.ACTIVE;
  }

  public boolean isActive() {
    return status == AccountStatus.ACTIVE;
  }

  public void changePassword(PasswordHash newPasswordHash) {
    credential.updatePassword(newPasswordHash);
  }

  public AccountId getId() {
    return id;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public AccountCredential getCredential() {
    return credential;
  }

  public EmailAddress getEmail() {
    return credential.getEmail();
  }
}
