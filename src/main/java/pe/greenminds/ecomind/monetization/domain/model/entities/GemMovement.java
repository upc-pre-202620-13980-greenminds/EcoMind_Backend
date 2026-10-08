package pe.greenminds.ecomind.monetization.domain.model.entities;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;

/** Immutable ledger entry. A reference id makes external reward delivery idempotent. */
public record GemMovement(
    UUID id,
    Long userId,
    GemMovementType type,
    GemMovementOrigin origin,
    int amount,
    int balanceAfter,
    UUID referenceId,
    Instant occurredAt) {
  public GemMovement {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    Objects.requireNonNull(type, "Movement type is required");
    Objects.requireNonNull(origin, "Movement origin is required");
    if (amount <= 0) throw new IllegalArgumentException("Gem amount must be positive");
    if (balanceAfter < 0) throw new IllegalArgumentException("Resulting balance cannot be negative");
    Objects.requireNonNull(referenceId, "Movement reference is required");
    Objects.requireNonNull(occurredAt, "Movement date is required");
  }
}
