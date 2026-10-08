package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.commandservices.CatalogCommandService;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.domain.repositories.CosmeticRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.GemPackageRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.MultiplierRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.StreakProtectorRepository;

@Service
@Transactional
public class CatalogCommandServiceImpl implements CatalogCommandService {
  private final CosmeticRepository cosmetics;
  private final MultiplierRepository multipliers;
  private final StreakProtectorRepository protectors;
  private final GemPackageRepository gemPackages;

  public CatalogCommandServiceImpl(
      CosmeticRepository cosmetics,
      MultiplierRepository multipliers,
      StreakProtectorRepository protectors,
      GemPackageRepository gemPackages) {
    this.cosmetics = cosmetics;
    this.multipliers = multipliers;
    this.protectors = protectors;
    this.gemPackages = gemPackages;
  }

  @Override
  public Cosmetic createCosmetic(
      String name, String description, int priceInGems, String type, String imageUrl) {
    CosmeticType cosmeticType;
    try {
      cosmeticType = CosmeticType.valueOf(type.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Cosmetic type must be AVATAR, HEAD, BODY or ACCESSORY");
    }
    return cosmetics.save(
        new Cosmetic(null, name, description, priceInGems, cosmeticType, imageUrl, true));
  }

  @Override
  public Multiplier createMultiplier(
      String name,
      String description,
      BigDecimal factor,
      int durationMinutes,
      int priceInGems) {
    return multipliers.save(
        new Multiplier(null, name, description, factor, durationMinutes, priceInGems, true));
  }

  @Override
  public StreakProtector createStreakProtector(
      String name, String description, int priceInGems) {
    return protectors.save(new StreakProtector(null, name, description, priceInGems, true));
  }

  @Override
  public GemPackage createGemPackage(
      String name, int gemAmount, BigDecimal price, String currency) {
    return gemPackages.save(
        new GemPackage(null, name, gemAmount, price, currency.trim().toUpperCase(Locale.ROOT), true));
  }
}
