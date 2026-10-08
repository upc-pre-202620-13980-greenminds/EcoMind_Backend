package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.EventPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.EventPersistenceRepository;

@Repository
public class EventRepositoryImpl implements EventRepository {
    private final EventPersistenceRepository repository;

    public EventRepositoryImpl(EventPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public Event save(Event event) {
        return EventPersistenceAssembler.toDomain(repository.save(EventPersistenceAssembler.toEntity(event)));
    }

    @Override
    public Optional<Event> findById(Long id) {
        return repository.findById(id).map(EventPersistenceAssembler::toDomain);
    }

    @Override
    public List<Event> findAll(Long communityId) {
        return (communityId == null ? repository.findAll() : repository.findByCommunityId(communityId)).stream()
                .map(EventPersistenceAssembler::toDomain).toList();
    }

    @Override
    public void delete(Event event) {
        repository.deleteById(event.getId());
    }
}
