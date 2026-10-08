package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPurchasePersistenceEntity;

public interface GemPurchasePersistenceRepository
    extends JpaRepository<GemPurchasePersistenceEntity, String> {
  Optional<GemPurchasePersistenceEntity> findByRequestId(String requestId);
  List<GemPurchasePersistenceEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
