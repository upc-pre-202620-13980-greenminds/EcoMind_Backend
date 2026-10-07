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
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.commands.ConfirmPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.entities.AccountCredential;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountStatus;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.repositories.AccountCredentialRepository;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.domain.repositories.PasswordResetTokenRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;
import pe.greenminds.ecomind.iam.domain.services.PasswordPolicy;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryCommandServiceImplTests {

  private static final EmailAddress EMAIL = new EmailAddress("camila@example.com");
  private static final AccountId ACCOUNT_ID = new AccountId(7L);
  private static final String RAW_TOKEN = "recovery-token";

  @Mock
  private AccountCredentialRepository accountCredentialRepository;
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock
  private PasswordHasher passwordHasher;
  @Mock
  private EmailService emailService;

  private PasswordRecoveryCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new PasswordRecoveryCommandServiceImpl(
            accountCredentialRepository,
            accountRepository,
            passwordResetTokenRepository,
            new PasswordPolicy(),
            passwordHasher,
            emailService);
  }

  @Test
  void requestStoresOnlyTheHashOfTheTokenAndEmailsTheToken() {
    when(accountCredentialRepository.findAccountByEmail(EMAIL)).thenReturn(Optional.of(account()));

    var result = service.handle(new RequestPasswordRecoveryCommand("camila@example.com"));

    assertThat(result.isSuccess()).isTrue();
    var tokenCaptor = ArgumentCaptor.forClass(String.class);
    var storedCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(emailService).sendPasswordRecoveryLink(eq(EMAIL), tokenCaptor.capture());
    verify(passwordResetTokenRepository).save(storedCaptor.capture());
    assertThat(storedCaptor.getValue().getTokenHash())
        .isEqualTo(TokenHash.of(tokenCaptor.getValue()));
    assertThat(storedCaptor.getValue().getTokenHash().value()).isNotEqualTo(tokenCaptor.getValue());
    assertThat(storedCaptor.getValue().getAccountId()).isEqualTo(ACCOUNT_ID);
  }

  @Test
  void requestForUnknownEmailSucceedsWithoutSendingAnything() {
    when(accountCredentialRepository.findAccountByEmail(EMAIL)).thenReturn(Optional.empty());

    var result = service.handle(new RequestPasswordRecoveryCommand("camila@example.com"));

    assertThat(result.isSuccess()).isTrue();
    verify(passwordResetTokenRepository, never()).save(any());
    verify(emailService, never()).sendPasswordRecoveryLink(any(), any());
  }

  @Test
  void confirmChangesThePasswordAndConsumesTheToken() {
    Account account = account();
    PasswordResetToken token = resetToken(Instant.now().plusSeconds(600), null);
    var newHash = new PasswordHash("new-hash");
    when(passwordResetTokenRepository.findByTokenHash(TokenHash.of(RAW_TOKEN)))
        .thenReturn(Optional.of(token));
    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
    when(passwordHasher.hash("NewGreenPlanet2026")).thenReturn(newHash);

    var result =
        service.handle(new ConfirmPasswordRecoveryCommand(RAW_TOKEN, "NewGreenPlanet2026"));

    assertThat(result.isSuccess()).isTrue();
    assertThat(account.getCredential().getPasswordHash()).isEqualTo(newHash);
    assertThat(token.getUsedAt()).isNotNull();
    verify(accountRepository).save(account);
    verify(passwordResetTokenRepository).save(token);
  }

  @Test
  void confirmFailsWhenTheTokenWasAlreadyUsed() {
    PasswordResetToken token = resetToken(Instant.now().plusSeconds(600), Instant.now());
    when(passwordResetTokenRepository.findByTokenHash(TokenHash.of(RAW_TOKEN)))
        .thenReturn(Optional.of(token));

    var result =
        service.handle(new ConfirmPasswordRecoveryCommand(RAW_TOKEN, "NewGreenPlanet2026"));

    assertThat(errorCodeOf(result)).isEqualTo("PASSWORD_RESET_TOKEN_INVALID");
    verify(accountRepository, never()).save(any());
  }

  @Test
  void confirmFailsWhenTheTokenHasExpired() {
    PasswordResetToken token = resetToken(Instant.now().minusSeconds(1), null);
    when(passwordResetTokenRepository.findByTokenHash(TokenHash.of(RAW_TOKEN)))
        .thenReturn(Optional.of(token));

    var result =
        service.handle(new ConfirmPasswordRecoveryCommand(RAW_TOKEN, "NewGreenPlanet2026"));

    assertThat(errorCodeOf(result)).isEqualTo("PASSWORD_RESET_TOKEN_INVALID");
    verify(accountRepository, never()).save(any());
  }

  @Test
  void confirmFailsWhenTheNewPasswordIsWeakWithoutConsumingTheToken() {
    var result = service.handle(new ConfirmPasswordRecoveryCommand(RAW_TOKEN, "weak"));

    assertThat(errorCodeOf(result)).isEqualTo("PASSWORD_POLICY_VIOLATION");
    verify(passwordResetTokenRepository, never()).findByTokenHash(any());
  }

  private static Account account() {
    return new Account(
        ACCOUNT_ID,
        AccountStatus.ACTIVE,
        new AccountCredential(EMAIL, new PasswordHash("old-hash")));
  }

  private static PasswordResetToken resetToken(Instant expiresAt, Instant usedAt) {
    return new PasswordResetToken(3L, ACCOUNT_ID, TokenHash.of(RAW_TOKEN), expiresAt, usedAt);
  }

  private static String errorCodeOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error().code();
  }
}
