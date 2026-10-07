package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenPersistenceEntity;

public interface PasswordResetTokenPersistenceRepository
    extends JpaRepository<PasswordResetTokenPersistenceEntity, Long> {

  Optional<PasswordResetTokenPersistenceEntity> findByTokenHash(String tokenHash);
}
