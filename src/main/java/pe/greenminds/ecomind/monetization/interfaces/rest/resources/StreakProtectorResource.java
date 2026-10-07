package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;

public record StreakProtectorResource(
    UUID id,
    String name,
    String description,
    int priceInGems) {

  public static StreakProtectorResource from(StreakProtector protector) {
    return new StreakProtectorResource(
        protector.id(), protector.name(), protector.description(), protector.priceInGems());
  }
}
