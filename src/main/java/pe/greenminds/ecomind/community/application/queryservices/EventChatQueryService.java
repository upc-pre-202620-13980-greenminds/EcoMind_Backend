package pe.greenminds.ecomind.community.application.queryservices;

import pe.greenminds.ecomind.community.domain.model.queries.GetEventChatMessagesQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventChatPage;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventChatQueryService {
    Result<EventChatPage, ApplicationError> handle(
            GetEventChatMessagesQuery query);
}