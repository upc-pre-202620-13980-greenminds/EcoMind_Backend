package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;

public interface GemMovementRepository {
  Optional<GemMovement> findByReferenceId(UUID referenceId);
  List<GemMovement> findRecentByUserId(Long userId);
  GemMovement save(GemMovement movement);
}
