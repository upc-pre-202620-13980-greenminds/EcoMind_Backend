package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.commands.CancelMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.FinishMinigameAttemptCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface MinigameAttemptCommandService {
    Result<MinigameAttempt, ApplicationError> handle(CreateMinigameAttemptCommand command);
    Result<MinigameAttempt, ApplicationError> handle(FinishMinigameAttemptCommand command);
    Result<MinigameAttempt, ApplicationError> handle(CancelMinigameAttemptCommand command);
}
