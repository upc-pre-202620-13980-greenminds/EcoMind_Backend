package pe.greenminds.ecomind.community.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;

public interface EventChatMessageRepository {
    EventChatMessage save(EventChatMessage message);

    List<EventChatMessage> findAfter(Long eventId, Long afterId);

    Optional<EventChatMessage> findByClientId(
            Long eventId, Long senderId, UUID clientId);

    void deleteByEventId(Long eventId);
}