package pe.greenminds.ecomind.monetization.application.commandservices;

import java.math.BigDecimal;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;

public interface CatalogCommandService {
  Cosmetic createCosmetic(String name, String description, int priceInGems, String type, String imageUrl);

  Multiplier createMultiplier(
      String name, String description, BigDecimal factor, int durationMinutes, int priceInGems);

  StreakProtector createStreakProtector(String name, String description, int priceInGems);

  GemPackage createGemPackage(String name, int gemAmount, BigDecimal price, String currency);
}
