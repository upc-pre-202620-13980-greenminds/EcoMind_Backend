package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;

public interface StreakProtectorRepository {
  StreakProtector save(StreakProtector protector);
  Optional<StreakProtector> findById(Long id);
  List<StreakProtector> findActive();
}
