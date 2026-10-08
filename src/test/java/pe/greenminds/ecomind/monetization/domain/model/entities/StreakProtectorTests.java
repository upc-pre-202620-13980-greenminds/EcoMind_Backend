package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import java.util.UUID;

class StreakProtectorTests {
  private static final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Test
  void createsAValidStreakProtector() {
    var protector = new StreakProtector(
        TEST_ID, "Streak Shield", "Protects one day of an active streak.", 100, true);

    assertEquals(TEST_ID, protector.id());
    assertEquals("Streak Shield", protector.name());
    assertEquals(100, protector.priceInGems());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(TEST_ID, " ", "Description", 100, true));
  }

  @Test
  void rejectsABlankDescription() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(TEST_ID, "Streak Shield", " ", 100, true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () ->
        new StreakProtector(TEST_ID, "Streak Shield", "Description", 0, true));
  }
}
