package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.EventChatMessagePersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.EventChatMessagePersistenceRepository;

@Repository
@Transactional
public class EventChatMessageRepositoryImpl implements EventChatMessageRepository {
    private final EventChatMessagePersistenceRepository repository;

    public EventChatMessageRepositoryImpl(
            EventChatMessagePersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public EventChatMessage save(EventChatMessage message) {
        var entity = EventChatMessagePersistenceAssembler.toEntity(message);
        return EventChatMessagePersistenceAssembler.toDomain(repository.save(entity));
    }

    @Override
    public List<EventChatMessage> findAfter(Long eventId, Long afterId) {
        return repository.findByEventIdAndIdGreaterThanOrderByIdAsc(
                        eventId, afterId, PageRequest.of(0, 50))
                .stream()
                .map(EventChatMessagePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<EventChatMessage> findByClientId(
            Long eventId, Long senderId, UUID clientId) {
        return repository.findByEventIdAndSenderIdAndClientId(
                        eventId, senderId, clientId)
                .map(EventChatMessagePersistenceAssembler::toDomain);
    }

    @Override
    public void deleteByEventId(Long eventId) {
        repository.deleteByEventId(eventId);
    }
}