package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;

public interface MultiplierRepository {
  Multiplier save(Multiplier multiplier);
  Optional<Multiplier> findById(UUID id);
  List<Multiplier> findActive();
}
