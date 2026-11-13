package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.monetization.application.commandservices.GemWalletCommandService;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.domain.repositories.CosmeticRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.MultiplierRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.StreakProtectorRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.UserCosmeticPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.ProcessedMonetizationRequestPersistenceRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.ProtectorInventoryPersistenceRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.UserCosmeticPersistenceRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.UserMultiplierPersistenceRepository;

class InventoryCommandServiceTests {

  @Test
  void equippingAHeadKeepsTheAvatarEquipped() {
    long userId = 42L;
    UUID avatarId = UUID.randomUUID();
    UUID hatId = UUID.randomUUID();
    var avatar = ownedCosmetic(userId, avatarId, true);
    var hat = ownedCosmetic(userId, hatId, false);
    var cosmetics = mock(CosmeticRepository.class);
    var owned = mock(UserCosmeticPersistenceRepository.class);
    when(cosmetics.findById(avatarId)).thenReturn(Optional.of(cosmetic(avatarId, CosmeticType.AVATAR)));
    when(cosmetics.findById(hatId)).thenReturn(Optional.of(cosmetic(hatId, CosmeticType.HEAD)));
    when(owned.findByUserIdAndCosmeticId(userId, hatId.toString())).thenReturn(Optional.of(hat));
    when(owned.findByUserId(userId)).thenReturn(List.of(avatar, hat));

    service(cosmetics, owned).equipCosmetic(userId, hatId, true);

    assertTrue(avatar.isEquipped());
    assertTrue(hat.isEquipped());
  }

  @Test
  void equippingAHeadUnequipsOnlyThePreviouslyEquippedHead() {
    long userId = 43L;
    UUID avatarId = UUID.randomUUID();
    UUID hatId = UUID.randomUUID();
    UUID bunId = UUID.randomUUID();
    var avatar = ownedCosmetic(userId, avatarId, true);
    var hat = ownedCosmetic(userId, hatId, true);
    var bun = ownedCosmetic(userId, bunId, false);
    var cosmetics = mock(CosmeticRepository.class);
    var owned = mock(UserCosmeticPersistenceRepository.class);
    when(cosmetics.findById(avatarId)).thenReturn(Optional.of(cosmetic(avatarId, CosmeticType.AVATAR)));
    when(cosmetics.findById(hatId)).thenReturn(Optional.of(cosmetic(hatId, CosmeticType.HEAD)));
    when(cosmetics.findById(bunId)).thenReturn(Optional.of(cosmetic(bunId, CosmeticType.HEAD)));
    when(owned.findByUserIdAndCosmeticId(userId, bunId.toString())).thenReturn(Optional.of(bun));
    when(owned.findByUserId(userId)).thenReturn(List.of(avatar, hat, bun));

    service(cosmetics, owned).equipCosmetic(userId, bunId, true);

    assertTrue(avatar.isEquipped());
    assertFalse(hat.isEquipped());
    assertTrue(bun.isEquipped());
  }

  private static InventoryCommandService service(
      CosmeticRepository cosmetics, UserCosmeticPersistenceRepository owned) {
    return new InventoryCommandService(
        cosmetics,
        mock(MultiplierRepository.class),
        mock(StreakProtectorRepository.class),
        owned,
        mock(ProtectorInventoryPersistenceRepository.class),
        mock(UserMultiplierPersistenceRepository.class),
        mock(GemWalletCommandService.class),
        mock(ProcessedMonetizationRequestPersistenceRepository.class));
  }

  private static UserCosmeticPersistenceEntity ownedCosmetic(
      long userId, UUID cosmeticId, boolean equipped) {
    var owned = new UserCosmeticPersistenceEntity();
    owned.setId(UUID.randomUUID().toString());
    owned.setUserId(userId);
    owned.setCosmeticId(cosmeticId.toString());
    owned.setEquipped(equipped);
    return owned;
  }

  private static Cosmetic cosmetic(UUID id, CosmeticType type) {
    return new Cosmetic(id, "Test cosmetic", "Test description", 100, type, "test_image", true);
  }
}
