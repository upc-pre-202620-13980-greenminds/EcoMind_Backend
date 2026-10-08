package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;

public record GemMovementResource(
    UUID id, GemMovementType type, GemMovementOrigin origin, int amount,
    int balanceAfter, Instant occurredAt) {
  public static GemMovementResource from(GemMovement movement) {
    return new GemMovementResource(movement.id(), movement.type(), movement.origin(),
        movement.amount(), movement.balanceAfter(), movement.occurredAt());
  }
}
