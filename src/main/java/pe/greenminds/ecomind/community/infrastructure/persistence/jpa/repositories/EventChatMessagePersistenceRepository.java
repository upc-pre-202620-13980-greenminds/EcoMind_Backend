package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventChatMessagePersistenceEntity;

public interface EventChatMessagePersistenceRepository
        extends JpaRepository<EventChatMessagePersistenceEntity, Long> {

    List<EventChatMessagePersistenceEntity> findByEventIdAndIdGreaterThanOrderByIdAsc(
            Long eventId, Long afterId, Pageable pageable);

    Optional<EventChatMessagePersistenceEntity> findByEventIdAndSenderIdAndClientId(
            Long eventId, Long senderId, UUID clientId);

    void deleteByEventId(Long eventId);
}