package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.UserMultiplierPersistenceEntity;

public record OwnedMultiplierResource(
    UUID id, UUID multiplierId, BigDecimal factor, Instant startsAt, Instant expiresAt) {
  public static OwnedMultiplierResource from(UserMultiplierPersistenceEntity entity) {
    return new OwnedMultiplierResource(
        UUID.fromString(entity.getId()), UUID.fromString(entity.getMultiplierId()),
        entity.getFactor(), entity.getStartsAt(), entity.getExpiresAt());
  }
}
