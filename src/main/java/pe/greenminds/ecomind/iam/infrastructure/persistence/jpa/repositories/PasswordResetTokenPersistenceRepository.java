package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenPersistenceEntity;

public interface PasswordResetTokenPersistenceRepository
    extends JpaRepository<PasswordResetTokenPersistenceEntity, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PasswordResetTokenPersistenceEntity> findByTokenHash(String tokenHash);
}
