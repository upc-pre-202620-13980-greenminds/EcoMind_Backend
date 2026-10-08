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

    public EventRegistrationRepositoryImpl(EventRegistrationPersistenceRepository r) {
        repository = r;
    }

    public EventRegistration save(EventRegistration r) {
        var entity = r.id() == null ? EventRegistrationPersistenceAssembler.toEntity(r)
                : repository.findById(r.id()).orElseThrow();
        if (r.status() == EventRegistrationStatus.CANCELLED)
            entity.cancel();
        return EventRegistrationPersistenceAssembler.toDomain(repository.save(entity));
    }

    public Optional<EventRegistration> findByEventIdAndUserId(Long e, Long u) {
        return repository.findByEventIdAndUserId(e, u).map(EventRegistrationPersistenceAssembler::toDomain);
    }

    public Optional<EventRegistration> findById(Long id) {
        return repository.findById(id).map(EventRegistrationPersistenceAssembler::toDomain);
    }

    public List<EventRegistration> findByEventId(Long id) {
        return repository.findByEventId(id).stream().map(EventRegistrationPersistenceAssembler::toDomain).toList();
    }
}
