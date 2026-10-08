package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.util.UUID;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.UserCosmeticPersistenceEntity;

public record OwnedCosmeticResource(UUID id, UUID cosmeticId, boolean equipped) {
  public static OwnedCosmeticResource from(UserCosmeticPersistenceEntity entity) {
    return new OwnedCosmeticResource(
        UUID.fromString(entity.getId()), UUID.fromString(entity.getCosmeticId()), entity.isEquipped());
  }
}
