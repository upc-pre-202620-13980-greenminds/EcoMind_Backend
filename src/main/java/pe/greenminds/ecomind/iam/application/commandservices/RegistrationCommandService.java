package pe.greenminds.ecomind.iam.application.commandservices;

import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.commands.SubmitRegistrationCommand;
import pe.greenminds.ecomind.iam.domain.model.commands.VerifyEmailCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface RegistrationCommandService {

  Result<PendingRegistration, ApplicationError> handle(SubmitRegistrationCommand command);

  Result<Account, ApplicationError> handle(VerifyEmailCommand command);
}
