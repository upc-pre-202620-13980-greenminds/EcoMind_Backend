package pe.greenminds.ecomind.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.iam.application.commandservices.AuthenticationCommandService;
import pe.greenminds.ecomind.iam.application.commandservices.SignInResult;
import pe.greenminds.ecomind.iam.application.internal.IamErrors;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.commands.SignInCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.services.AuthenticationService;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

  private final AuthenticationService authenticationService;
  private final TokenService tokenService;

  public AuthenticationCommandServiceImpl(
      AuthenticationService authenticationService, TokenService tokenService) {
    this.authenticationService = authenticationService;
    this.tokenService = tokenService;
  }

  @Override
  @Transactional(readOnly = true)
  public Result<SignInResult, ApplicationError> handle(SignInCommand command) {
    EmailAddress email;
    try {
      email = new EmailAddress(command.email());
    } catch (IllegalArgumentException ex) {
      // A malformed email gets the same answer as wrong credentials.
      return Result.failure(IamErrors.invalidCredentials());
    }
    return authenticationService
        .authenticate(email, command.password())
        .map(user -> new SignInResult(user, tokenService.issueAccessToken(user)))
        .<Result<SignInResult, ApplicationError>>map(Result::success)
        .orElseGet(() -> Result.failure(IamErrors.invalidCredentials()));
  }
}
