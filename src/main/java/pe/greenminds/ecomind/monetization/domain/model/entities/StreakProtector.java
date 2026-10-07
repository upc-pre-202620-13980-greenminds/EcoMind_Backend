package pe.greenminds.ecomind.monetization.domain.model.entities;

/** Immutable streak protector definition exposed by the store catalog. */
public record StreakProtector(
    Long id,
    String name,
    String description,
    int priceInGems,
    boolean active) {

  public StreakProtector {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Streak protector id must be positive");
    }
    if (name == null || name.isBlank() || name.length() > 120) {
      throw new IllegalArgumentException(
          "Streak protector name is required, up to 120 characters");
    }
    if (description == null || description.isBlank() || description.length() > 500) {
      throw new IllegalArgumentException(
          "Streak protector description is required, up to 500 characters");
    }
    if (priceInGems <= 0) {
      throw new IllegalArgumentException("Streak protector price must be positive");
    }
  }
}
