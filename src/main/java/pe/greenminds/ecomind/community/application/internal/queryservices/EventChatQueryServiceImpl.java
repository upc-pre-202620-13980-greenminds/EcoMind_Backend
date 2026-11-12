package pe.greenminds.ecomind.community.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.queryservices.EventChatAccessService;
import pe.greenminds.ecomind.community.application.queryservices.EventChatQueryService;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventChatMessagesQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventChatPage;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class EventChatQueryServiceImpl implements EventChatQueryService {
    private final EventChatAccessService access;
    private final EventChatMessageRepository messages;

    public EventChatQueryServiceImpl(
            EventChatAccessService access,
            EventChatMessageRepository messages) {
        this.access = access;
        this.messages = messages;
    }

    @Override
    @Transactional
    public Result<EventChatPage, ApplicationError> handle(
            GetEventChatMessagesQuery query) {
        if (query.afterId() == null || query.afterId() < 0) {
            return Result.failure(ApplicationError.validationError(
                    "Event chat cursor",
                    "After id must be zero or greater"));
        }

        return access.requireParticipant(query.eventId(), query.requestedBy())
                .map(event -> new EventChatPage(
                        event.getId(),
                        event.getAuthorId(),
                        event.getChatExpiresAt(),
                        messages.findAfter(event.getId(), query.afterId())));
    }
}