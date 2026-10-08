package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;

public record GemPackageResource(
    UUID id,
    String name,
    int gemAmount,
    BigDecimal price,
    String currency) {

  public static GemPackageResource from(GemPackage gemPackage) {
    return new GemPackageResource(
        gemPackage.id(), gemPackage.name(), gemPackage.gemAmount(), gemPackage.price(),
        gemPackage.currency());
  }
}
