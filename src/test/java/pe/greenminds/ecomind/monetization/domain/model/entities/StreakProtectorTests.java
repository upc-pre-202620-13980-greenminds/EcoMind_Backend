package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StreakProtectorTests {

  @Test
  void createsAValidStreakProtector() {
    var protector = new StreakProtector(
        1L, "Streak Shield", "Protects one day of an active streak.", 100, true);

    assertEquals(1L, protector.id());
    assertEquals("Streak Shield", protector.name());
    assertEquals(100, protector.priceInGems());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(1L, " ", "Description", 100, true));
  }

  @Test
  void rejectsABlankDescription() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(1L, "Streak Shield", " ", 100, true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(1L, "Streak Shield", "Description", 0, true));
  }

  @Test
  void rejectsANonPositiveId() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(0L, "Streak Shield", "Description", 100, true));
  }
}
