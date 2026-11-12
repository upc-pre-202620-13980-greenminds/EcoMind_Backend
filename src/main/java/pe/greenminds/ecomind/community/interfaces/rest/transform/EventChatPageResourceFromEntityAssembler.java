package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.valueobjects.EventChatPage;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventChatPageResource;

public final class EventChatPageResourceFromEntityAssembler {
    private EventChatPageResourceFromEntityAssembler() {
    }

    public static EventChatPageResource toResourceFromEntity(
            EventChatPage page, Long requestedBy) {
        return new EventChatPageResource(
                page.eventId(),
                page.authorId(),
                page.expiresAt(),
                page.messages().stream()
                        .map(message -> EventChatMessageResourceFromEntityAssembler
                                .toResourceFromEntity(message, requestedBy))
                        .toList());
    }
}