package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.AccountPersistenceEntity;

public interface AccountPersistenceRepository
    extends JpaRepository<AccountPersistenceEntity, Long> {

  Optional<AccountPersistenceEntity> findByCredentialEmail(String email);

  boolean existsByCredentialEmail(String email);
}
