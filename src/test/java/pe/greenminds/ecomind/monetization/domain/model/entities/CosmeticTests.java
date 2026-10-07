package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;

class CosmeticTests {
  private static final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Test
  void createsAValidCosmetic() {
    var cosmetic = new Cosmetic(
        TEST_ID,
        "Recycling Hat",
        "A green hat made for recycling champions.",
        80,
        CosmeticType.HEAD,
        "https://example.com/recycling-hat.png",
        true);

    assertEquals(TEST_ID, cosmetic.id());
    assertEquals("Recycling Hat", cosmetic.name());
    assertEquals(80, cosmetic.priceInGems());
    assertEquals(CosmeticType.HEAD, cosmetic.type());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        TEST_ID, " ", "Description", 80, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        TEST_ID, "Recycling Hat", "Description", 0, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void requiresAType() {
    assertThrows(NullPointerException.class, () -> new Cosmetic(
        TEST_ID, "Recycling Hat", "Description", 80, null,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsABlankDescription() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        TEST_ID, "Recycling Hat", " ", 80, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsABlankImageReference() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        TEST_ID, "Recycling Hat", "Description", 80, CosmeticType.HEAD, " ", true));
  }

  @Test
  void acceptsAnAndroidDrawableNameAsImageReference() {
    var cosmetic = new Cosmetic(
        TEST_ID,
        "Sonic",
        "Fast blue hedgehog avatar.",
        350,
        CosmeticType.AVATAR,
        "sonic",
        true);

    assertEquals("sonic", cosmetic.imageUrl());
  }
}
