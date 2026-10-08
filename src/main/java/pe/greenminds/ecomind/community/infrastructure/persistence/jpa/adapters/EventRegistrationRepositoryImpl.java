package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.EventRegistrationPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.EventRegistrationPersistenceRepository;

@Repository
public class EventRegistrationRepositoryImpl implements EventRegistrationRepository {
    private final EventRegistrationPersistenceRepository repository;

    public EventRegistrationRepositoryImpl(EventRegistrationPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public EventRegistration save(EventRegistration eventRegistration) {
        var entity = eventRegistration.id() == null ? EventRegistrationPersistenceAssembler.toEntity(eventRegistration)
                : repository.findById(eventRegistration.id()).orElseThrow();
        if (eventRegistration.status() == EventRegistrationStatus.CANCELLED)
            entity.cancel();
        return EventRegistrationPersistenceAssembler.toDomain(repository.save(entity));
    }

    @Override
    public Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId) {
        return repository.findByEventIdAndUserId(eventId, userId).map(EventRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<EventRegistration> findById(Long id) {
        return repository.findById(id).map(EventRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public List<EventRegistration> findByEventId(Long id) {
        return repository.findByEventId(id).stream().map(EventRegistrationPersistenceAssembler::toDomain).toList();
    }
}
