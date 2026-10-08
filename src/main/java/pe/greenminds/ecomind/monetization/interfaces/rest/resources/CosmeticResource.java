package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;

public record CosmeticResource(
    UUID id,
    String name,
    String description,
    int priceInGems,
    String type,
    String imageReference) {

  public static CosmeticResource from(Cosmetic cosmetic) {
    return new CosmeticResource(
        cosmetic.id(), cosmetic.name(), cosmetic.description(), cosmetic.priceInGems(),
        cosmetic.type().name(), cosmetic.imageUrl());
  }
}
