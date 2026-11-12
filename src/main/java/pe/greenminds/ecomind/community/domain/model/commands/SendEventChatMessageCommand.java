package pe.greenminds.ecomind.community.domain.model.commands;

import java.util.UUID;

public record SendEventChatMessageCommand(
        Long eventId,
        Long requestedBy,
        String text,
        UUID clientId) {
}