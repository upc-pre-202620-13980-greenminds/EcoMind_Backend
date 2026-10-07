package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPackagePersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.MultiplierPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.StreakProtectorPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.GemPackagePersistenceRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.MultiplierPersistenceRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.StreakProtectorPersistenceRepository;

class StoreCatalogRepositoryImplTests {
  private static final UUID MULTIPLIER_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
  private static final UUID PROTECTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
  private static final UUID PACKAGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000022");

  @Test
  void savesAndMapsAMultiplier() {
    var persistence = mock(MultiplierPersistenceRepository.class);
    when(persistence.save(any(MultiplierPersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, MultiplierPersistenceEntity.class); entity.setId(MULTIPLIER_ID.toString()); return entity;
    });
    var saved = new MultiplierRepositoryImpl(persistence).save(new Multiplier(
        null, "Double XP", "Doubles experience.", new BigDecimal("2.0"), 30, 150, true));
    assertEquals(MULTIPLIER_ID, saved.id()); assertEquals(new BigDecimal("2.0"), saved.factor());
  }

  @Test
  void savesAndMapsAStreakProtector() {
    var persistence = mock(StreakProtectorPersistenceRepository.class);
    when(persistence.save(any(StreakProtectorPersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, StreakProtectorPersistenceEntity.class); entity.setId(PROTECTOR_ID.toString()); return entity;
    });
    var saved = new StreakProtectorRepositoryImpl(persistence).save(new StreakProtector(
        null, "Streak Shield", "Protects one day.", 100, true));
    assertEquals(PROTECTOR_ID, saved.id()); assertEquals(100, saved.priceInGems());
  }

  @Test
  void savesAndMapsAGemPackage() {
    var persistence = mock(GemPackagePersistenceRepository.class);
    when(persistence.save(any(GemPackagePersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, GemPackagePersistenceEntity.class); entity.setId(PACKAGE_ID.toString()); return entity;
    });
    var saved = new GemPackageRepositoryImpl(persistence).save(new GemPackage(
        null, "Starter Gems", 100, new BigDecimal("3.99"), "PEN", true));
    assertEquals(PACKAGE_ID, saved.id()); assertEquals("PEN", saved.currency());
  }
}
