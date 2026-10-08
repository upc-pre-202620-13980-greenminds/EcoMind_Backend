package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.entities.AccountCredential;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountStatus;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.AccountCredentialPersistenceEntity;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.AccountPersistenceEntity;

public final class AccountPersistenceAssembler {

  private AccountPersistenceAssembler() {
  }

  public static Account toDomainFromPersistence(AccountPersistenceEntity entity) {
    AccountCredentialPersistenceEntity credential = entity.getCredential();
    return new Account(
        new AccountId(entity.getId()),
        AccountStatus.valueOf(entity.getStatus()),
        new AccountCredential(
            new EmailAddress(credential.getEmail()),
            new PasswordHash(credential.getPasswordHash())));
  }

  public static AccountPersistenceEntity toPersistenceFromDomain(Account account) {
    AccountPersistenceEntity entity = new AccountPersistenceEntity();
    AccountCredentialPersistenceEntity credential = new AccountCredentialPersistenceEntity();
    credential.setAccount(entity);
    credential.setEmail(account.getEmail().value());
    entity.setCredential(credential);
    copyMutableState(account, entity);
    return entity;
  }

  /** Copies what can change after creation: the status and the password hash. */
  public static void copyMutableState(Account account, AccountPersistenceEntity entity) {
    entity.setStatus(account.getStatus().name());
    entity.getCredential().setPasswordHash(account.getCredential().getPasswordHash().value());
  }
}
