package pe.greenminds.ecomind.community.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record EventChatPageResource(
        Long event_id,
        Long author_id,
        Instant expires_at,
        List<EventChatMessageResource> messages) {
}