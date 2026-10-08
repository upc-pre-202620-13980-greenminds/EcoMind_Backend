package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.EventRegistrationPersistenceEntity;

public interface EventRegistrationPersistenceRepository
        extends JpaRepository<EventRegistrationPersistenceEntity, Long> {
    List<EventRegistrationPersistenceEntity> findByEventId(Long eventId);

    Optional<EventRegistrationPersistenceEntity> findByEventIdAndUserId(Long eventId, Long userId);
}
