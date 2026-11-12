package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EventChatMessage(
        Long id,
        Long eventId,
        Long senderId,
        String senderName,
        String text,
        Instant sentAt,
        UUID clientId) {

    public EventChatMessage {
        Objects.requireNonNull(eventId, "Event id is required");
        Objects.requireNonNull(senderId, "Sender id is required");
        Objects.requireNonNull(senderName, "Sender name is required");
        Objects.requireNonNull(text, "Message text is required");
        Objects.requireNonNull(sentAt, "Message date is required");
        Objects.requireNonNull(clientId, "Client id is required");

        if (eventId <= 0 || senderId <= 0) {
            throw new IllegalArgumentException("Identifiers must be positive");
        }

        text = text.strip();

        if (text.isEmpty() || text.length() > 2000) {
            throw new IllegalArgumentException(
                    "Message text must contain between 1 and 2000 characters");
        }
    }
}