package pe.greenminds.ecomind.iam.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

public interface AccountRepository {

  /** Saves the account with its credential and returns it with its id assigned. */
  Account save(Account account);

  Optional<Account> findById(AccountId accountId);

  boolean existsByEmail(EmailAddress email);
}
