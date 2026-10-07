package pe.greenminds.ecomind.iam.application.internal.commandservices;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.iam.application.commandservices.PasswordRecoveryCommandService;
import pe.greenminds.ecomind.iam.application.internal.IamErrors;
import pe.greenminds.ecomind.iam.application.internal.SecureTokenGenerator;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.commands.ConfirmPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.repositories.AccountCredentialRepository;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.domain.repositories.PasswordResetTokenRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;
import pe.greenminds.ecomind.iam.domain.services.PasswordPolicy;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class PasswordRecoveryCommandServiceImpl implements PasswordRecoveryCommandService {

  private final AccountCredentialRepository accountCredentialRepository;
  private final AccountRepository accountRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordPolicy passwordPolicy;
  private final PasswordHasher passwordHasher;
  private final EmailService emailService;

  public PasswordRecoveryCommandServiceImpl(
      AccountCredentialRepository accountCredentialRepository,
      AccountRepository accountRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      PasswordPolicy passwordPolicy,
      PasswordHasher passwordHasher,
      EmailService emailService) {
    this.accountCredentialRepository = accountCredentialRepository;
    this.accountRepository = accountRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordPolicy = passwordPolicy;
    this.passwordHasher = passwordHasher;
    this.emailService = emailService;
  }

  @Override
  @Transactional
  public Result<EmailAddress, ApplicationError> handle(RequestPasswordRecoveryCommand command) {
    EmailAddress email = new EmailAddress(command.email());
    // The result is the same whether or not the email has an account; only the email is sent.
    accountCredentialRepository
        .findAccountByEmail(email)
        .filter(Account::isActive)
        .ifPresent(account -> issueRecoveryToken(account, email));
    return Result.success(email);
  }

  @Override
  @Transactional
  public Result<AccountId, ApplicationError> handle(ConfirmPasswordRecoveryCommand command) {
    if (!passwordPolicy.isSatisfiedBy(command.newPassword())) {
      return Result.failure(IamErrors.passwordPolicyViolation());
    }

    var resetToken = passwordResetTokenRepository.findByTokenHash(TokenHash.of(command.token()));
    if (resetToken.isEmpty() || !resetToken.get().consume(Instant.now())) {
      return Result.failure(IamErrors.passwordResetTokenInvalid());
    }
    PasswordResetToken consumedToken = resetToken.get();

    var account = accountRepository.findById(consumedToken.getAccountId());
    if (account.isEmpty()) {
      return Result.failure(IamErrors.passwordResetTokenInvalid());
    }

    account.get().changePassword(passwordHasher.hash(command.newPassword()));
    accountRepository.save(account.get());
    passwordResetTokenRepository.save(consumedToken);
    return Result.success(consumedToken.getAccountId());
  }

  private void issueRecoveryToken(Account account, EmailAddress email) {
    String recoveryToken = SecureTokenGenerator.newRecoveryToken();
    passwordResetTokenRepository.save(
        PasswordResetToken.issue(account.getId(), TokenHash.of(recoveryToken), Instant.now()));
    emailService.sendPasswordRecoveryLink(email, recoveryToken);
  }
}
