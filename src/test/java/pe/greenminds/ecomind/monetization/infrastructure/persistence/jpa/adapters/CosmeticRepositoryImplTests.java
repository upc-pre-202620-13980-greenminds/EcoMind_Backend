package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.CosmeticPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.CosmeticPersistenceRepository;

class CosmeticRepositoryImplTests {
  private static final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

  private CosmeticPersistenceRepository persistenceRepository;
  private CosmeticRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    persistenceRepository = mock(CosmeticPersistenceRepository.class);
    repository = new CosmeticRepositoryImpl(persistenceRepository);
  }

  @Test
  void savesAndMapsACosmetic() {
    when(persistenceRepository.save(any(CosmeticPersistenceEntity.class))).thenAnswer(invocation -> {
      var entity = invocation.getArgument(0, CosmeticPersistenceEntity.class);
      entity.setId(TEST_ID.toString());
      return entity;
    });

    var saved = repository.save(new Cosmetic(
        null, "Sonic", "Fast blue hedgehog avatar.", 350,
        CosmeticType.AVATAR, "sonic", true));

    assertEquals(TEST_ID, saved.id());
    assertEquals("Sonic", saved.name());
    assertEquals("sonic", saved.imageUrl());
    verify(persistenceRepository).save(any(CosmeticPersistenceEntity.class));
  }

  @Test
  void findsACosmeticById() {
    when(persistenceRepository.findById(TEST_ID.toString()))
        .thenReturn(Optional.of(entity(TEST_ID, true)));

    var result = repository.findById(TEST_ID);

    assertTrue(result.isPresent());
    assertEquals(CosmeticType.AVATAR, result.orElseThrow().type());
  }

  @Test
  void listsOnlyActiveCosmeticsFromPersistence() {
    when(persistenceRepository.findAllByActiveTrueOrderByNameAsc())
        .thenReturn(List.of(entity(TEST_ID, true), entity(UUID.randomUUID(), true)));

    var cosmetics = repository.findActive();

    assertEquals(2, cosmetics.size());
    assertTrue(cosmetics.stream().allMatch(Cosmetic::active));
  }

  private static CosmeticPersistenceEntity entity(UUID id, boolean active) {
    var entity = new CosmeticPersistenceEntity();
    entity.setId(id.toString());
    entity.setName("Sonic");
    entity.setDescription("Fast blue hedgehog avatar.");
    entity.setPriceInGems(350);
    entity.setType(CosmeticType.AVATAR.name());
    entity.setImageUrl("sonic");
    entity.setActive(active);
    return entity;
  }
}
