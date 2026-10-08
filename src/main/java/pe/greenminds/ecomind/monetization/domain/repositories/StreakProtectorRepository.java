package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;

public interface StreakProtectorRepository {
  StreakProtector save(StreakProtector protector);
  Optional<StreakProtector> findById(UUID id);
  List<StreakProtector> findActive();
}
