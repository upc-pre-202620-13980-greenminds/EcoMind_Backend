package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.adapters;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.repositories.AccountCredentialRepository;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.assemblers.AccountPersistenceAssembler;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.AccountPersistenceEntity;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories.AccountPersistenceRepository;

/**
 * Persists accounts together with their credentials, which are stored in their own table.
 */
@Repository
public class AccountCredentialRepositoryImpl
    implements AccountRepository, AccountCredentialRepository {

  private final AccountPersistenceRepository accountPersistenceRepository;

  public AccountCredentialRepositoryImpl(
      AccountPersistenceRepository accountPersistenceRepository) {
    this.accountPersistenceRepository = accountPersistenceRepository;
  }

  @Override
  public Account save(Account account) {
    AccountPersistenceEntity entity;
    if (account.getId() == null) {
      entity = AccountPersistenceAssembler.toPersistenceFromDomain(account);
    } else {
      entity = accountPersistenceRepository.findById(account.getId().value()).orElseThrow();
      AccountPersistenceAssembler.copyMutableState(account, entity);
    }
    return AccountPersistenceAssembler.toDomainFromPersistence(
        accountPersistenceRepository.save(entity));
  }

  @Override
  public Optional<Account> findById(AccountId accountId) {
    return accountPersistenceRepository
        .findById(accountId.value())
        .map(AccountPersistenceAssembler::toDomainFromPersistence);
  }

  @Override
  public boolean existsByEmail(EmailAddress email) {
    return accountPersistenceRepository.existsByCredentialEmail(email.value());
  }

  @Override
  public Optional<Account> findAccountByEmail(EmailAddress email) {
    return accountPersistenceRepository
        .findByCredentialEmail(email.value())
        .map(AccountPersistenceAssembler::toDomainFromPersistence);
  }
}
