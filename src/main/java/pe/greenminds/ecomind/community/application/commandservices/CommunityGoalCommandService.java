package pe.greenminds.ecomind.community.application.commandservices;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.model.commands.CreateCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.commands.JoinCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.commands.RecordGoalQuestCompletionCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface CommunityGoalCommandService {
    Result<CommunityGoal, ApplicationError> handle(CreateCommunityGoalCommand command);

    Result<CommunityGoal, ApplicationError> handle(JoinCommunityGoalCommand command);

    void handle(RecordGoalQuestCompletionCommand command);

    void leaveCommunity(Long communityId, Long userId);
}