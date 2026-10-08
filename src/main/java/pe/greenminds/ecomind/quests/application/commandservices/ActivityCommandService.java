package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateActivityCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteActivityCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateActivityCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface ActivityCommandService {
    Result<Activity, ApplicationError> handle(CreateActivityCommand command);

    Result<Activity, ApplicationError> handle(DeleteActivityCommand command);

    Result<Activity, ApplicationError> handle(UpdateActivityCommand command);
}
