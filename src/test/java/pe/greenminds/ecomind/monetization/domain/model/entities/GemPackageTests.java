package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GemPackageTests {

  @Test
  void createsAValidGemPackage() {
    var gemPackage = new GemPackage(
        1L, "Starter Gems", 100, new BigDecimal("3.99"), "PEN", true);

    assertEquals(1L, gemPackage.id());
    assertEquals(100, gemPackage.gemAmount());
    assertEquals(new BigDecimal("3.99"), gemPackage.price());
    assertEquals("PEN", gemPackage.currency());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () -> new GemPackage(
        1L, " ", 100, new BigDecimal("3.99"), "PEN", true));
  }

  @Test
  void rejectsANonPositiveGemAmount() {
    assertThrows(IllegalArgumentException.class, () -> new GemPackage(
        1L, "Starter Gems", 0, new BigDecimal("3.99"), "PEN", true));
  }

  @Test
  void requiresAPrice() {
    assertThrows(NullPointerException.class, () ->
        new GemPackage(1L, "Starter Gems", 100, null, "PEN", true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () -> new GemPackage(
        1L, "Starter Gems", 100, BigDecimal.ZERO, "PEN", true));
  }

  @Test
  void rejectsAnInvalidCurrency() {
    assertThrows(IllegalArgumentException.class, () -> new GemPackage(
        1L, "Starter Gems", 100, new BigDecimal("3.99"), "SOL", true));
  }

  @Test
  void rejectsANonPositiveId() {
    assertThrows(IllegalArgumentException.class, () -> new GemPackage(
        0L, "Starter Gems", 100, new BigDecimal("3.99"), "PEN", true));
  }
}
