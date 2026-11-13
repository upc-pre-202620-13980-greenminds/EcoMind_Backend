package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventChatMessagePersistenceEntity;

public final class EventChatMessagePersistenceAssembler {
    private EventChatMessagePersistenceAssembler() {
    }

    public static EventChatMessage toDomain(
            EventChatMessagePersistenceEntity entity) {
        return new EventChatMessage(
                entity.getId(),
                entity.getEventId(),
                entity.getSenderId(),
                entity.getSenderName(),
                entity.getText(),
                entity.getSentAt(),
                entity.getClientId());
    }

    public static EventChatMessagePersistenceEntity toEntity(
            EventChatMessage message) {
        return new EventChatMessagePersistenceEntity(
                message.eventId(),
                message.senderId(),
                message.senderName(),
                message.text(),
                message.sentAt(),
                message.clientId());
    }
}