package pe.greenminds.ecomind.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.application.outboundservices.UsersContextGateway;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.commands.SubmitRegistrationCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.VerifyEmailCommand;
import pe.greenminds.ecomind.iam.domain.model.entities.AccountCredential;
import pe.greenminds.ecomind.iam.domain.model.entities.EmailVerification;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountStatus;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.domain.repositories.PendingRegistrationRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;
import pe.greenminds.ecomind.iam.domain.services.PasswordPolicy;
import pe.greenminds.ecomind.iam.domain.services.RegistrationPolicy;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@ExtendWith(MockitoExtension.class)
class RegistrationCommandServiceImplTests {

  private static final EmailAddress EMAIL = new EmailAddress("camila@example.com");
  private static final PasswordHash PASSWORD_HASH = new PasswordHash("hashed-password");
  private static final String CODE = "482913";

  @Mock
  private PendingRegistrationRepository pendingRegistrationRepository;
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private PasswordHasher passwordHasher;
  @Mock
  private EmailService emailService;
  @Mock
  private UsersContextGateway usersContextGateway;

  private RegistrationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    var registrationPolicy = new RegistrationPolicy(accountRepository, new PasswordPolicy());
    service =
        new RegistrationCommandServiceImpl(
            pendingRegistrationRepository,
            accountRepository,
            registrationPolicy,
            passwordHasher,
            emailService,
            usersContextGateway);
  }

  @Test
  void submitRegistrationStoresPendingRegistrationAndSendsTheCode() {
    when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(passwordHasher.hash("GreenPlanet2026")).thenReturn(PASSWORD_HASH);
    when(pendingRegistrationRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result =
        service.handle(
            new SubmitRegistrationCommand(
                "Camila Torres", " Camila@Example.com ", "GreenPlanet2026", SocialRole.STUDENT));

    assertThat(result.isSuccess()).isTrue();
    PendingRegistration pending = result.toOptional().orElseThrow();
    assertThat(pending.getEmail()).isEqualTo(EMAIL);
    assertThat(pending.getPasswordHash()).isEqualTo(PASSWORD_HASH);
    assertThat(pending.getSocialRole()).isEqualTo(SocialRole.STUDENT);
    assertThat(pending.isVerified()).isFalse();

    // The code that is emailed is the one whose hash was stored, and it has six digits.
    var codeCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendVerificationCode(eq(EMAIL), eq("Camila Torres"), codeCaptor.capture());
    assertThat(codeCaptor.getValue()).matches("\\d{6}");
    assertThat(pending.getEmailVerification().getTokenHash())
        .isEqualTo(TokenHash.of(codeCaptor.getValue()));
    verify(pendingRegistrationRepository).deleteByEmail(EMAIL);
  }

  @Test
  void submitRegistrationFailsWhenTheEmailAlreadyHasAnAccount() {
    when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

    var result =
        service.handle(
            new SubmitRegistrationCommand(
                "Camila Torres", "camila@example.com", "GreenPlanet2026", SocialRole.STUDENT));

    assertThat(errorCodeOf(result)).isEqualTo("EMAIL_CONFLICT");
    verify(pendingRegistrationRepository, never()).save(any());
    verify(emailService, never()).sendVerificationCode(any(), any(), any());
  }

  @Test
  void submitRegistrationFailsWhenThePasswordIsWeak() {
    var result =
        service.handle(
            new SubmitRegistrationCommand(
                "Camila Torres", "camila@example.com", "short", SocialRole.PARENT));

    assertThat(errorCodeOf(result)).isEqualTo("PASSWORD_POLICY_VIOLATION");
    verify(pendingRegistrationRepository, never()).save(any());
  }

  @Test
  void verifyEmailCreatesTheAccountAndRequestsTheProfile() {
    PendingRegistration pending = pendingRegistration(Instant.now().plusSeconds(600), 0);
    when(pendingRegistrationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pending));
    when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(accountRepository.save(any())).thenAnswer(call -> withId(call.getArgument(0), 7L));

    var result = service.handle(new VerifyEmailCommand("camila@example.com", CODE));

    assertThat(result.isSuccess()).isTrue();
    Account account = result.toOptional().orElseThrow();
    assertThat(account.isActive()).isTrue();
    assertThat(account.getEmail()).isEqualTo(EMAIL);
    assertThat(pending.isVerified()).isTrue();
    verify(usersContextGateway).createProfile(new AccountId(7L), "Camila Torres", SocialRole.PARENT);
  }

  @Test
  void verifyEmailWithWrongCodeCountsTheAttemptAndCreatesNoAccount() {
    PendingRegistration pending = pendingRegistration(Instant.now().plusSeconds(600), 0);
    when(pendingRegistrationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pending));

    var result = service.handle(new VerifyEmailCommand("camila@example.com", "000000"));

    assertThat(errorCodeOf(result)).isEqualTo("VERIFICATION_CODE_INVALID");
    assertThat(pending.getEmailVerification().getAttempts()).isEqualTo(1);
    verify(pendingRegistrationRepository).save(pending);
    verify(accountRepository, never()).save(any());
    verify(usersContextGateway, never()).createProfile(any(), any(), any());
  }

  @Test
  void verifyEmailFailsWhenTheCodeHasExpired() {
    PendingRegistration pending = pendingRegistration(Instant.now().minusSeconds(1), 0);
    when(pendingRegistrationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pending));

    var result = service.handle(new VerifyEmailCommand("camila@example.com", CODE));

    assertThat(errorCodeOf(result)).isEqualTo("VERIFICATION_CODE_EXPIRED");
    verify(accountRepository, never()).save(any());
  }

  @Test
  void verifyEmailFailsAfterTooManyAttemptsEvenWithTheRightCode() {
    PendingRegistration pending =
        pendingRegistration(Instant.now().plusSeconds(600), EmailVerification.MAX_ATTEMPTS);
    when(pendingRegistrationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(pending));

    var result = service.handle(new VerifyEmailCommand("camila@example.com", CODE));

    assertThat(errorCodeOf(result)).isEqualTo("VERIFICATION_ATTEMPTS_EXCEEDED");
    verify(accountRepository, never()).save(any());
  }

  @Test
  void verifyEmailFailsWhenThereIsNoPendingRegistration() {
    when(pendingRegistrationRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    var result = service.handle(new VerifyEmailCommand("camila@example.com", CODE));

    assertThat(errorCodeOf(result)).isEqualTo("VERIFICATION_CODE_INVALID");
  }

  private static PendingRegistration pendingRegistration(Instant expiresAt, int attempts) {
    return new PendingRegistration(
        1L,
        "Camila Torres",
        EMAIL,
        PASSWORD_HASH,
        SocialRole.PARENT,
        expiresAt,
        Instant.now(),
        new EmailVerification(TokenHash.of(CODE), expiresAt, null, attempts));
  }

  private static Account withId(Account account, long id) {
    return new Account(
        new AccountId(id),
        AccountStatus.valueOf(account.getStatus().name()),
        new AccountCredential(account.getEmail(), account.getCredential().getPasswordHash()));
  }

  private static String errorCodeOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error().code();
  }
}
