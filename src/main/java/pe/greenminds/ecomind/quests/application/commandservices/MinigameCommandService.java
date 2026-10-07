package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateMinigameCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteMinigameCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface MinigameCommandService {
    Result<Minigame, ApplicationError> handle(CreateMinigameCommand command);
    Result<Minigame, ApplicationError> handle(DeleteMinigameCommand command);
}
