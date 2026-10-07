package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.util.List;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.Store;

public record StoreCatalogResource(
    List<CosmeticResource> cosmetics,
    List<MultiplierResource> multipliers,
    List<StreakProtectorResource> streakProtectors,
    List<GemPackageResource> gemPackages) {

  public static StoreCatalogResource from(Store store) {
    return new StoreCatalogResource(
        store.cosmetics().stream().map(CosmeticResource::from).toList(),
        store.multipliers().stream().map(MultiplierResource::from).toList(),
        store.streakProtectors().stream().map(StreakProtectorResource::from).toList(),
        store.gemPackages().stream().map(GemPackageResource::from).toList());
  }
}
