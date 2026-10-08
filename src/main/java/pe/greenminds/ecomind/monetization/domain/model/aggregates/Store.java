package pe.greenminds.ecomind.monetization.domain.model.aggregates;

import java.util.List;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;

/** Read model containing the active products offered by the EcoMind store. */
public record Store(
    List<Cosmetic> cosmetics,
    List<Multiplier> multipliers,
    List<StreakProtector> streakProtectors,
    List<GemPackage> gemPackages) {

  public Store {
    cosmetics = List.copyOf(cosmetics);
    multipliers = List.copyOf(multipliers);
    streakProtectors = List.copyOf(streakProtectors);
    gemPackages = List.copyOf(gemPackages);
  }
}
