package pe.greenminds.ecomind.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.iam.application.commandservices.SignInResult;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.commands.SignInCommand;
import pe.greenminds.ecomind.iam.domain.model.entities.AccountCredential;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccessToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountStatus;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.repositories.AccountCredentialRepository;
import pe.greenminds.ecomind.iam.domain.services.AuthenticationService;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.ErrorType;
import pe.greenminds.ecomind.shared.application.result.Result;

@ExtendWith(MockitoExtension.class)
class AuthenticationCommandServiceImplTests {

  private static final EmailAddress EMAIL = new EmailAddress("camila@example.com");
  private static final PasswordHash PASSWORD_HASH = new PasswordHash("hashed-password");

  @Mock
  private AccountCredentialRepository accountCredentialRepository;
  @Mock
  private PasswordHasher passwordHasher;
  @Mock
  private TokenService tokenService;

  private AuthenticationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new AuthenticationCommandServiceImpl(
            new AuthenticationService(accountCredentialRepository, passwordHasher), tokenService);
  }

  @Test
  void signInReturnsAnAccessTokenForValidCredentials() {
    var accessToken = new AccessToken("signed.jwt.token", Instant.now().plusSeconds(3600));
    when(accountCredentialRepository.findAccountByEmail(EMAIL))
        .thenReturn(Optional.of(account(AccountStatus.ACTIVE)));
    when(passwordHasher.matches("GreenPlanet2026", PASSWORD_HASH)).thenReturn(true);
    when(tokenService.issueAccessToken(new AuthenticatedUser(new AccountId(7L), EMAIL)))
        .thenReturn(accessToken);

    var result = service.handle(new SignInCommand("Camila@Example.com", "GreenPlanet2026"));

    SignInResult signIn = result.toOptional().orElseThrow();
    assertThat(signIn.accessToken()).isEqualTo(accessToken);
    assertThat(signIn.authenticatedUser().accountId()).isEqualTo(new AccountId(7L));
  }

  @Test
  void signInFailsWithTheSameErrorForWrongPasswordAndUnknownEmail() {
    when(accountCredentialRepository.findAccountByEmail(EMAIL))
        .thenReturn(Optional.of(account(AccountStatus.ACTIVE)));
    when(passwordHasher.matches("wrong-password1", PASSWORD_HASH)).thenReturn(false);
    var wrongPassword = service.handle(new SignInCommand("camila@example.com", "wrong-password1"));

    when(accountCredentialRepository.findAccountByEmail(new EmailAddress("nobody@example.com")))
        .thenReturn(Optional.empty());
    var unknownEmail = service.handle(new SignInCommand("nobody@example.com", "GreenPlanet2026"));

    assertThat(errorOf(wrongPassword)).isEqualTo(errorOf(unknownEmail));
    assertThat(errorOf(wrongPassword).code()).isEqualTo("INVALID_CREDENTIALS");
    assertThat(errorOf(wrongPassword).type()).isEqualTo(ErrorType.UNAUTHORIZED);
    verify(tokenService, never()).issueAccessToken(any());
  }

  @Test
  void signInFailsWhenTheAccountIsNotActive() {
    when(accountCredentialRepository.findAccountByEmail(EMAIL))
        .thenReturn(Optional.of(account(AccountStatus.INACTIVE)));

    var result = service.handle(new SignInCommand("camila@example.com", "GreenPlanet2026"));

    assertThat(errorOf(result).code()).isEqualTo("INVALID_CREDENTIALS");
    verify(tokenService, never()).issueAccessToken(any());
  }

  @Test
  void signInWithMalformedEmailIsTreatedAsInvalidCredentials() {
    var result = service.handle(new SignInCommand("not-an-email", "GreenPlanet2026"));

    assertThat(errorOf(result).code()).isEqualTo("INVALID_CREDENTIALS");
  }

  private static Account account(AccountStatus status) {
    return new Account(new AccountId(7L), status, new AccountCredential(EMAIL, PASSWORD_HASH));
  }

  private static ApplicationError errorOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error();
  }
}
