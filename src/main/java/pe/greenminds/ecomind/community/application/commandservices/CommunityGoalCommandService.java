package pe.greenminds.ecomind.community.application.commandservices;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.model.commands.CreateCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.commands.IncrementCommunityGoalCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface CommunityGoalCommandService {
    Result<CommunityGoal, ApplicationError> handle(CreateCommunityGoalCommand command);

    Result<CommunityGoal, ApplicationError> handle(IncrementCommunityGoalCommand command);
}
