package pe.greenminds.ecomind.community.domain.model.valueobjects;

import java.time.Instant;
import java.util.List;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;

public record EventChatPage(
        Long eventId,
        Long authorId,
        Instant expiresAt,
        List<EventChatMessage> messages) {

    public EventChatPage {
        messages = List.copyOf(messages);
    }
}