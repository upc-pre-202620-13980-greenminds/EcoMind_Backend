package pe.greenminds.ecomind.monetization.application.internal.queryservices;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.queryservices.StoreQueryService;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.Store;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;
import pe.greenminds.ecomind.monetization.domain.repositories.CosmeticRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.GemPackageRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.MultiplierRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.StreakProtectorRepository;

@Service
@Transactional(readOnly = true)
public class StoreQueryServiceImpl implements StoreQueryService {
  private final CosmeticRepository cosmetics;
  private final MultiplierRepository multipliers;
  private final StreakProtectorRepository streakProtectors;
  private final GemPackageRepository gemPackages;

  public StoreQueryServiceImpl(
      CosmeticRepository cosmetics,
      MultiplierRepository multipliers,
      StreakProtectorRepository streakProtectors,
      GemPackageRepository gemPackages) {
    this.cosmetics = cosmetics;
    this.multipliers = multipliers;
    this.streakProtectors = streakProtectors;
    this.gemPackages = gemPackages;
  }

  @Override
  public Store getCatalog() {
    return new Store(
        cosmetics.findActive(),
        multipliers.findActive(),
        streakProtectors.findActive(),
        gemPackages.findActive());
  }

  @Override
  public Optional<Cosmetic> findCosmeticById(UUID id) {
    return cosmetics.findById(id).filter(Cosmetic::active);
  }

  @Override
  public Optional<Multiplier> findMultiplierById(UUID id) {
    return multipliers.findById(id).filter(Multiplier::active);
  }

  @Override
  public Optional<StreakProtector> findStreakProtectorById(UUID id) {
    return streakProtectors.findById(id).filter(StreakProtector::active);
  }

  @Override
  public Optional<GemPackage> findGemPackageById(UUID id) {
    return gemPackages.findById(id).filter(GemPackage::active);
  }
}
