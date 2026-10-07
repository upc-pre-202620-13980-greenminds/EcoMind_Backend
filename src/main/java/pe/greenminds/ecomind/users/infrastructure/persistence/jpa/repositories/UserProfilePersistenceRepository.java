package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.UserProfilePersistenceEntity;

public interface UserProfilePersistenceRepository
    extends JpaRepository<UserProfilePersistenceEntity, Long> {
}
