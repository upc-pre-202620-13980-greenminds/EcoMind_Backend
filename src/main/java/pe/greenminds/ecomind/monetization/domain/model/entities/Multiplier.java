package pe.greenminds.ecomind.monetization.domain.model.entities;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Immutable experience multiplier definition exposed by the store catalog. */
public record Multiplier(
    UUID id,
    String name,
    String description,
    BigDecimal factor,
    int durationMinutes,
    int priceInGems,
    boolean active) {

  public Multiplier {
    if (name == null || name.isBlank() || name.length() > 120) {
      throw new IllegalArgumentException("Multiplier name is required, up to 120 characters");
    }
    if (description == null || description.isBlank() || description.length() > 500) {
      throw new IllegalArgumentException(
          "Multiplier description is required, up to 500 characters");
    }
    Objects.requireNonNull(factor, "Multiplier factor is required");
    if (factor.compareTo(BigDecimal.ONE) <= 0) {
      throw new IllegalArgumentException("Multiplier factor must be greater than one");
    }
    if (durationMinutes <= 0) {
      throw new IllegalArgumentException("Multiplier duration must be positive");
    }
    if (priceInGems <= 0) {
      throw new IllegalArgumentException("Multiplier price must be positive");
    }
  }
}
