package pe.greenminds.ecomind.iam.domain.services;

import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.repositories.AccountCredentialRepository;

/**
 * Checks an email and password. The result is empty for any failure, so the caller cannot tell
 * whether the email or the password was wrong.
 */
@Service
public class AuthenticationService {

  private final AccountCredentialRepository accountCredentialRepository;
  private final PasswordHasher passwordHasher;

  public AuthenticationService(
      AccountCredentialRepository accountCredentialRepository, PasswordHasher passwordHasher) {
    this.accountCredentialRepository = accountCredentialRepository;
    this.passwordHasher = passwordHasher;
  }

  public Optional<AuthenticatedUser> authenticate(EmailAddress email, String rawPassword) {
    return accountCredentialRepository
        .findAccountByEmail(email)
        .filter(Account::isActive)
        .filter(account -> account.getCredential().matchesPassword(rawPassword, passwordHasher))
        .map(account -> new AuthenticatedUser(account.getId(), account.getEmail()));
  }
}
