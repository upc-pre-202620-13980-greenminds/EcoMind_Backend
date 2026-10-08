package pe.greenminds.ecomind.monetization.application.queryservices;

import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.Store;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;

public interface StoreQueryService {
  Store getCatalog();

  Optional<Cosmetic> findCosmeticById(UUID id);

  Optional<Multiplier> findMultiplierById(UUID id);

  Optional<StreakProtector> findStreakProtectorById(UUID id);

  Optional<GemPackage> findGemPackageById(UUID id);
}
