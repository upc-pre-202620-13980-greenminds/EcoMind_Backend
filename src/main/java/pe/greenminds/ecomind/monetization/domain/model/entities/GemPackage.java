package pe.greenminds.ecomind.monetization.domain.model.entities;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/** Immutable package of gems purchasable with real money. */
public record GemPackage(
    Long id,
    String name,
    int gemAmount,
    BigDecimal price,
    String currency,
    boolean active) {

  public GemPackage {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Gem package id must be positive");
    }
    if (name == null || name.isBlank() || name.length() > 120) {
      throw new IllegalArgumentException("Gem package name is required, up to 120 characters");
    }
    if (gemAmount <= 0) {
      throw new IllegalArgumentException("Gem amount must be positive");
    }
    Objects.requireNonNull(price, "Gem package price is required");
    if (price.signum() <= 0) {
      throw new IllegalArgumentException("Gem package price must be positive");
    }
    if (currency == null || !currency.matches("[A-Z]{3}")) {
      throw new IllegalArgumentException("Currency must be a three-letter ISO 4217 code");
    }
    try {
      Currency.getInstance(currency);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Currency must be a valid ISO 4217 code", exception);
    }
  }
}
