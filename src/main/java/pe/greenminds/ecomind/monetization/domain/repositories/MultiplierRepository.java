package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;

public interface MultiplierRepository {
  Multiplier save(Multiplier multiplier);
  Optional<Multiplier> findById(Long id);
  List<Multiplier> findActive();
}
