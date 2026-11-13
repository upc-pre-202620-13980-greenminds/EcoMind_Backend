package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventChatMessageResource;

public final class EventChatMessageResourceFromEntityAssembler {
    private EventChatMessageResourceFromEntityAssembler() {
    }

    public static EventChatMessageResource toResourceFromEntity(
            EventChatMessage message, Long requestedBy) {
        return new EventChatMessageResource(
                message.id(),
                message.eventId(),
                message.senderId(),
                message.senderName(),
                message.text(),
                message.sentAt(),
                message.clientId(),
                message.senderId().equals(requestedBy));
    }
}