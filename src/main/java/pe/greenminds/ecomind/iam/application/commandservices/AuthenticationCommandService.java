package pe.greenminds.ecomind.iam.application.commandservices;

import pe.greenminds.ecomind.iam.domain.model.commands.SignInCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface AuthenticationCommandService {

  Result<SignInResult, ApplicationError> handle(SignInCommand command);
}
