package pe.greenminds.ecomind.community.application.commandservices;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;
import pe.greenminds.ecomind.community.domain.model.commands.SendEventChatMessageCommand;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventChatCommandService {
    Result<EventChatMessage, ApplicationError> handle(
            SendEventChatMessageCommand command);
}