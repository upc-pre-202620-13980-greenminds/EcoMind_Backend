package pe.greenminds.ecomind.iam.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

public interface AccountCredentialRepository {

  /** Finds the account whose credential uses the given normalized email. */
  Optional<Account> findAccountByEmail(EmailAddress email);
}
