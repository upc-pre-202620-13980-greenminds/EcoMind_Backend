package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.util.UUID;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.ProtectorInventoryPersistenceEntity;

public record OwnedProtectorResource(UUID id, UUID protectorId, int quantity) {
  public static OwnedProtectorResource from(ProtectorInventoryPersistenceEntity entity) {
    return new OwnedProtectorResource(
        UUID.fromString(entity.getId()), UUID.fromString(entity.getProtectorId()), entity.getQuantity());
  }
}
