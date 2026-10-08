package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MultiplierTests {
  private static final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Test
  void createsAValidMultiplier() {
    var multiplier = new Multiplier(
        TEST_ID,
        "Double XP",
        "Doubles experience earned for thirty minutes.",
        new BigDecimal("2.0"),
        30,
        150,
        true);

    assertEquals(TEST_ID, multiplier.id());
    assertEquals(new BigDecimal("2.0"), multiplier.factor());
    assertEquals(30, multiplier.durationMinutes());
    assertEquals(150, multiplier.priceInGems());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        TEST_ID, " ", "Description", new BigDecimal("2.0"), 30, 150, true));
  }

  @Test
  void rejectsABlankDescription() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        TEST_ID, "Double XP", " ", new BigDecimal("2.0"), 30, 150, true));
  }

  @Test
  void requiresAFactor() {
    assertThrows(NullPointerException.class, () -> new Multiplier(
        TEST_ID, "Double XP", "Description", null, 30, 150, true));
  }

  @Test
  void rejectsAFactorNotGreaterThanOne() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        TEST_ID, "Double XP", "Description", BigDecimal.ONE, 30, 150, true));
  }

  @Test
  void rejectsANonPositiveDuration() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        TEST_ID, "Double XP", "Description", new BigDecimal("2.0"), 0, 150, true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () -> new Multiplier(
        TEST_ID, "Double XP", "Description", new BigDecimal("2.0"), 30, 0, true));
  }
}
