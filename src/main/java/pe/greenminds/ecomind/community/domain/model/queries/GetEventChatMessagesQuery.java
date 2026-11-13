package pe.greenminds.ecomind.community.domain.model.queries;

public record GetEventChatMessagesQuery(
        Long eventId,
        Long requestedBy,
        Long afterId) {
}