package pe.greenminds.ecomind.monetization.domain.model.entities;

import java.util.Objects;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;

/** Immutable cosmetic definition exposed by the store catalog. */
public record Cosmetic(
    Long id,
    String name,
    String description,
    int priceInGems,
    CosmeticType type,
    String imageUrl,
    boolean active) {

  public Cosmetic {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Cosmetic id must be positive");
    }
    if (name == null || name.isBlank() || name.length() > 120) {
      throw new IllegalArgumentException("Cosmetic name is required, up to 120 characters");
    }
    if (description == null || description.isBlank() || description.length() > 500) {
      throw new IllegalArgumentException(
          "Cosmetic description is required, up to 500 characters");
    }
    if (priceInGems <= 0) {
      throw new IllegalArgumentException("Cosmetic price must be positive");
    }
    Objects.requireNonNull(type, "Cosmetic type is required");
    if (imageUrl == null || imageUrl.isBlank() || imageUrl.length() > 500) {
      throw new IllegalArgumentException("Cosmetic image URL is required, up to 500 characters");
    }
  }
}
