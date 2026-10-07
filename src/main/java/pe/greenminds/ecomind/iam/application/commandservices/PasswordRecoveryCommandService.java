package pe.greenminds.ecomind.iam.application.commandservices;

import pe.greenminds.ecomind.iam.domain.model.commands.ConfirmPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface PasswordRecoveryCommandService {

  /** Succeeds for any well-formed email, whether or not it belongs to an account. */
  Result<EmailAddress, ApplicationError> handle(RequestPasswordRecoveryCommand command);

  Result<AccountId, ApplicationError> handle(ConfirmPasswordRecoveryCommand command);
}
