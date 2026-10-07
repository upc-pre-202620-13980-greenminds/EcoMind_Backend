package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

  @Test
  void savesAndMapsAMultiplier() {
    var persistence = mock(MultiplierPersistenceRepository.class);
    when(persistence.save(any(MultiplierPersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, MultiplierPersistenceEntity.class); entity.setId(1L); return entity;
    });
    var saved = new MultiplierRepositoryImpl(persistence).save(new Multiplier(
        null, "Double XP", "Doubles experience.", new BigDecimal("2.0"), 30, 150, true));
    assertEquals(1L, saved.id()); assertEquals(new BigDecimal("2.0"), saved.factor());
  }

  @Test
  void savesAndMapsAStreakProtector() {
    var persistence = mock(StreakProtectorPersistenceRepository.class);
    when(persistence.save(any(StreakProtectorPersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, StreakProtectorPersistenceEntity.class); entity.setId(2L); return entity;
    });
    var saved = new StreakProtectorRepositoryImpl(persistence).save(new StreakProtector(
        null, "Streak Shield", "Protects one day.", 100, true));
    assertEquals(2L, saved.id()); assertEquals(100, saved.priceInGems());
  }

  @Test
  void savesAndMapsAGemPackage() {
    var persistence = mock(GemPackagePersistenceRepository.class);
    when(persistence.save(any(GemPackagePersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, GemPackagePersistenceEntity.class); entity.setId(3L); return entity;
    });
    var saved = new GemPackageRepositoryImpl(persistence).save(new GemPackage(
        null, "Starter Gems", 100, new BigDecimal("3.99"), "PEN", true));
    assertEquals(3L, saved.id()); assertEquals("PEN", saved.currency());
  }
}
