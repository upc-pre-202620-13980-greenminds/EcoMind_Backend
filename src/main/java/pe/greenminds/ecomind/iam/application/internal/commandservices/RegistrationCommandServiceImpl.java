package pe.greenminds.ecomind.iam.application.internal.commandservices;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.iam.application.commandservices.RegistrationCommandService;
import pe.greenminds.ecomind.iam.application.internal.IamErrors;
import pe.greenminds.ecomind.iam.application.internal.SecureTokenGenerator;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.application.outboundservices.UsersContextGateway;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.commands.SubmitRegistrationCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.VerifyEmailCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.VerificationOutcome;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.domain.repositories.PendingRegistrationRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;
import pe.greenminds.ecomind.iam.domain.services.RegistrationPolicy;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class RegistrationCommandServiceImpl implements RegistrationCommandService {

  private final PendingRegistrationRepository pendingRegistrationRepository;
  private final AccountRepository accountRepository;
  private final RegistrationPolicy registrationPolicy;
  private final PasswordHasher passwordHasher;
  private final EmailService emailService;
  private final UsersContextGateway usersContextGateway;

  public RegistrationCommandServiceImpl(
      PendingRegistrationRepository pendingRegistrationRepository,
      AccountRepository accountRepository,
      RegistrationPolicy registrationPolicy,
      PasswordHasher passwordHasher,
      EmailService emailService,
      UsersContextGateway usersContextGateway) {
    this.pendingRegistrationRepository = pendingRegistrationRepository;
    this.accountRepository = accountRepository;
    this.registrationPolicy = registrationPolicy;
    this.passwordHasher = passwordHasher;
    this.emailService = emailService;
    this.usersContextGateway = usersContextGateway;
  }

  @Override
  @Transactional
  public Result<PendingRegistration, ApplicationError> handle(SubmitRegistrationCommand command) {
    EmailAddress email = new EmailAddress(command.email());
    if (!registrationPolicy.isPasswordAcceptable(command.password())) {
      return Result.failure(IamErrors.passwordPolicyViolation());
    }
    if (!registrationPolicy.isEmailAvailable(email)) {
      return Result.failure(IamErrors.emailAlreadyRegistered());
    }

    // Submitting again replaces the previous attempt and sends a new code.
    pendingRegistrationRepository.deleteByEmail(email);

    String verificationCode = SecureTokenGenerator.newVerificationCode();
    PasswordHash passwordHash = passwordHasher.hash(command.password());
    PendingRegistration pendingRegistration =
        pendingRegistrationRepository.save(
            PendingRegistration.create(
                command.name(),
                email,
                passwordHash,
                command.socialRole(),
                TokenHash.of(verificationCode),
                Instant.now()));

    emailService.sendVerificationCode(email, pendingRegistration.getName(), verificationCode);
    return Result.success(pendingRegistration);
  }

  @Override
  @Transactional
  public Result<Account, ApplicationError> handle(VerifyEmailCommand command) {
    EmailAddress email = new EmailAddress(command.email());
    var pending = pendingRegistrationRepository.findByEmail(email);
    if (pending.isEmpty()) {
      // Same answer as a wrong code, so the endpoint does not reveal which emails are pending.
      return Result.failure(IamErrors.verificationCodeInvalid());
    }

    PendingRegistration pendingRegistration = pending.get();
    VerificationOutcome outcome =
        pendingRegistration.verifyEmail(TokenHash.of(command.code()), Instant.now());
    // Saved even when the code is wrong: failed attempts must be counted.
    pendingRegistrationRepository.save(pendingRegistration);

    if (outcome != VerificationOutcome.VERIFIED) {
      return Result.failure(toError(outcome));
    }
    // The email could have been registered by someone else while this one was pending.
    if (!registrationPolicy.isEmailAvailable(email)) {
      return Result.failure(IamErrors.emailAlreadyRegistered());
    }

    Account account = accountRepository.save(pendingRegistration.createAccount());
    usersContextGateway.createProfile(
        account.getId(), pendingRegistration.getName(), pendingRegistration.getSocialRole());
    return Result.success(account);
  }

  private static ApplicationError toError(VerificationOutcome outcome) {
    return switch (outcome) {
      case EXPIRED -> IamErrors.verificationCodeExpired();
      case TOO_MANY_ATTEMPTS -> IamErrors.verificationAttemptsExceeded();
      default -> IamErrors.verificationCodeInvalid();
    };
  }
}
