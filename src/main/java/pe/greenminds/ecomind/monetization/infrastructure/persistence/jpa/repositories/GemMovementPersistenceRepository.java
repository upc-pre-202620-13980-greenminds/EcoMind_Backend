package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemMovementPersistenceEntity;

public interface GemMovementPersistenceRepository
    extends JpaRepository<GemMovementPersistenceEntity, String> {
  Optional<GemMovementPersistenceEntity> findByReferenceId(String referenceId);
  List<GemMovementPersistenceEntity> findTop100ByUserIdOrderByOccurredAtDesc(Long userId);
}
