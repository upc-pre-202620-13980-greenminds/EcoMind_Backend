package pe.greenminds.ecomind.iam.domain.services;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;

/**
 * Rules a registration must meet before it is accepted and before the account is created.
 */
@Service
public class RegistrationPolicy {

  private final AccountRepository accountRepository;
  private final PasswordPolicy passwordPolicy;

  public RegistrationPolicy(AccountRepository accountRepository, PasswordPolicy passwordPolicy) {
    this.accountRepository = accountRepository;
    this.passwordPolicy = passwordPolicy;
  }

  public boolean isPasswordAcceptable(String rawPassword) {
    return passwordPolicy.isSatisfiedBy(rawPassword);
  }

  /** An email can belong to one account only. */
  public boolean isEmailAvailable(EmailAddress email) {
    return !accountRepository.existsByEmail(email);
  }
}
