package pe.greenminds.ecomind.monetization.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.domain.repositories.CosmeticRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.GemPackageRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.MultiplierRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.StreakProtectorRepository;

class StoreQueryServiceImplTests {

  @Test
  void returnsAllActiveCatalogSections() {
    var cosmetics = mock(CosmeticRepository.class);
    var multipliers = mock(MultiplierRepository.class);
    var protectors = mock(StreakProtectorRepository.class);
    var packages = mock(GemPackageRepository.class);
    when(cosmetics.findActive()).thenReturn(List.of(new Cosmetic(
        UUID.randomUUID(), "Leaf Avatar", "A green leaf avatar.", 80,
        CosmeticType.AVATAR, "avatar_leaf", true)));
    when(multipliers.findActive()).thenReturn(List.of(new Multiplier(
        UUID.randomUUID(), "Double XP", "Doubles experience.",
        new BigDecimal("2.0"), 30, 150, true)));
    when(protectors.findActive()).thenReturn(List.of(new StreakProtector(
        UUID.randomUUID(), "Streak Shield", "Protects one day.", 100, true)));
    when(packages.findActive()).thenReturn(List.of(new GemPackage(
        UUID.randomUUID(), "Starter Gems", 100, new BigDecimal("3.99"), "PEN", true)));

    var store = new StoreQueryServiceImpl(cosmetics, multipliers, protectors, packages)
        .getCatalog();

    assertEquals(1, store.cosmetics().size());
    assertEquals(1, store.multipliers().size());
    assertEquals(1, store.streakProtectors().size());
    assertEquals(1, store.gemPackages().size());
  }
}
