package pe.greenminds.ecomind.monetization.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.queryservices.StoreQueryService;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.Store;
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
}
