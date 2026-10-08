package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;

public interface GemPackageRepository {
  GemPackage save(GemPackage gemPackage);
  Optional<GemPackage> findById(UUID id);
  List<GemPackage> findActive();
}
