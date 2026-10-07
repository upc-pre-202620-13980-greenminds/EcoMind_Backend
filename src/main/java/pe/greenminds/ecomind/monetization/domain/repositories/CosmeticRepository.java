package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;

/** Persistence port for the cosmetic catalog. */
public interface CosmeticRepository {
  Cosmetic save(Cosmetic cosmetic);

  Optional<Cosmetic> findById(UUID id);

  List<Cosmetic> findActive();
}
