package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateActivityUserCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.SubmitActivityUserCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface ActivityUserCommandService {
    Result<ActivityUser, ApplicationError> handle(CreateActivityUserCommand command);

    Result<ActivityUser, ApplicationError> handle(SubmitActivityUserCommand command);
}
