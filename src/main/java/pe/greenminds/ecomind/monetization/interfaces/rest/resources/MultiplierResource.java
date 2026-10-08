package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;

public record MultiplierResource(
    UUID id,
    String name,
    String description,
    BigDecimal factor,
    int durationMinutes,
    int priceInGems) {

  public static MultiplierResource from(Multiplier multiplier) {
    return new MultiplierResource(
        multiplier.id(), multiplier.name(), multiplier.description(), multiplier.factor(),
        multiplier.durationMinutes(), multiplier.priceInGems());
  }
}
