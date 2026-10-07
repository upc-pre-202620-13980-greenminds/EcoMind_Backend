package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeletePendingCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.StartCollabQuestSessionCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface CollabQuestSessionCommandService {
    Result<CollabQuestSession, ApplicationError> handle(CreateCollabQuestSessionCommand command);
    Result<CollabQuestSession, ApplicationError> handle(StartCollabQuestSessionCommand command);
    Result<CollabQuestSession, ApplicationError> handle(DeletePendingCollabQuestSessionCommand command);
}
