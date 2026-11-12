package pe.greenminds.ecomind.community.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record EventChatMessageResource(
        Long id,
        Long event_id,
        Long sender_id,
        String sender_name,
        String text,
        Instant sent_at,
        UUID client_id,
        boolean is_mine) {
}