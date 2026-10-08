package pe.greenminds.ecomind.quests.application.commandservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.ArchiveQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.PublishQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateQuestCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface QuestCommandService {

    Result<Quest, ApplicationError> handle(CreateQuestCommand command);

    Result<Quest, ApplicationError> handle(ArchiveQuestCommand command);

    Result<Quest, ApplicationError> handle(PublishQuestCommand command);

    Result<Quest, ApplicationError> handle(UpdateQuestCommand command);
}
