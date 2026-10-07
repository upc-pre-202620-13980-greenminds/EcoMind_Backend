package pe.greenminds.ecomind.monetization.domain.model.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;

class CosmeticTests {

  @Test
  void createsAValidCosmetic() {
    var cosmetic = new Cosmetic(
        1L,
        "Recycling Hat",
        "A green hat made for recycling champions.",
        80,
        CosmeticType.HEAD,
        "https://example.com/recycling-hat.png",
        true);

    assertEquals(1L, cosmetic.id());
    assertEquals("Recycling Hat", cosmetic.name());
    assertEquals(80, cosmetic.priceInGems());
    assertEquals(CosmeticType.HEAD, cosmetic.type());
  }

  @Test
  void rejectsABlankName() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        1L, " ", "Description", 80, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsANonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        1L, "Recycling Hat", "Description", 0, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void requiresAType() {
    assertThrows(NullPointerException.class, () -> new Cosmetic(
        1L, "Recycling Hat", "Description", 80, null,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsANonPositiveId() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        0L, "Recycling Hat", "Description", 80, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsABlankDescription() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        1L, "Recycling Hat", " ", 80, CosmeticType.HEAD,
        "https://example.com/recycling-hat.png", true));
  }

  @Test
  void rejectsABlankImageReference() {
    assertThrows(IllegalArgumentException.class, () -> new Cosmetic(
        1L, "Recycling Hat", "Description", 80, CosmeticType.HEAD, " ", true));
  }

  @Test
  void acceptsAnAndroidDrawableNameAsImageReference() {
    var cosmetic = new Cosmetic(
        1L,
        "Sonic",
        "Fast blue hedgehog avatar.",
        350,
        CosmeticType.AVATAR,
        "sonic",
        true);

    assertEquals("sonic", cosmetic.imageUrl());
  }
}
