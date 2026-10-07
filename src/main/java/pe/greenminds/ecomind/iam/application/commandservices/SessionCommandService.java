package pe.greenminds.ecomind.iam.application.commandservices;

import pe.greenminds.ecomind.iam.domain.model.commands.LogoutCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface SessionCommandService {

  Result<AccountId, ApplicationError> handle(LogoutCommand command);
}
