package pe.greenminds.ecomind.community.application.commandservices;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.commands.CancelEventRegistrationCommand;
import pe.greenminds.ecomind.community.domain.model.commands.RegisterForEventCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventRegistrationCommandService {
    Result<EventRegistration, ApplicationError> handle(RegisterForEventCommand command);

    Result<Void, ApplicationError> handle(CancelEventRegistrationCommand command);
}
