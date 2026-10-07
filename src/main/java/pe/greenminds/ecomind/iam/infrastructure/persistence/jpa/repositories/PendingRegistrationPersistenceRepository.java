package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.PendingRegistrationPersistenceEntity;

public interface PendingRegistrationPersistenceRepository
    extends JpaRepository<PendingRegistrationPersistenceEntity, Long> {

  Optional<PendingRegistrationPersistenceEntity> findByEmail(String email);
}
