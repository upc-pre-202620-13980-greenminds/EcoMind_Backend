package pe.greenminds.ecomind.community.application.commandservices;

import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.commands.CreateEventCommand;
import pe.greenminds.ecomind.community.domain.model.commands.DeleteEventCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventCommandService {
    Result<Event, ApplicationError> handle(CreateEventCommand command);

    Result<Void, ApplicationError> handle(DeleteEventCommand command);
}
