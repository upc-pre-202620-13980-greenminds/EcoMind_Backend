package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MultiplierTests {

  @Test
  void createsAValidMultiplier() {
    var multiplier = new Multiplier(
        1L,
        "Double XP",
        "Doubles experience earned for thirty minutes.",
        new BigDecimal("2.0"),
        30,
        150,
        true);

    assertEquals(1L, multiplier.id());
    assertEquals(new BigDecimal("2.0"), multiplier.factor());
    assertEquals(30, multiplier.durationMinutes());
    assertEquals(150, multiplier.priceInGems());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        1L, " ", "Description", new BigDecimal("2.0"), 30, 150, true));
  }

  @Test
  void rejectsABlankDescription() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        1L, "Double XP", " ", new BigDecimal("2.0"), 30, 150, true));
  }

  @Test
  void requiresAFactor() {
    assertThrows(NullPointerException.class, () -> new Multiplier(
        1L, "Double XP", "Description", null, 30, 150, true));
  }

  @Test
  void rejectsAFactorNotGreaterThanOne() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        1L, "Double XP", "Description", BigDecimal.ONE, 30, 150, true));
  }

  @Test
  void rejectsANonPositiveDuration() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        1L, "Double XP", "Description", new BigDecimal("2.0"), 0, 150, true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        1L, "Double XP", "Description", new BigDecimal("2.0"), 30, 0, true));
  }

  @Test
  void rejectsANonPositiveId() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        0L, "Double XP", "Description", new BigDecimal("2.0"), 30, 150, true));
  }
}
