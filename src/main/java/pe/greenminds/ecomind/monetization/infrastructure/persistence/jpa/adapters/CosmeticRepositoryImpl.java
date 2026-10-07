package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.monetization.domain.model.entities.Cosmetic;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.domain.repositories.CosmeticRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.CosmeticPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.CosmeticPersistenceRepository;

@Repository
public class CosmeticRepositoryImpl implements CosmeticRepository {

  private final CosmeticPersistenceRepository persistenceRepository;

  public CosmeticRepositoryImpl(CosmeticPersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
  }

  @Override
  public Cosmetic save(Cosmetic cosmetic) {
    var id = cosmetic.id() == null ? UUID.randomUUID() : cosmetic.id();
    var entity = cosmetic.id() == null
        ? new CosmeticPersistenceEntity()
        : persistenceRepository.findById(id.toString())
            .orElseGet(CosmeticPersistenceEntity::new);
    entity.setId(id.toString());
    entity.setName(cosmetic.name());
    entity.setDescription(cosmetic.description());
    entity.setPriceInGems(cosmetic.priceInGems());
    entity.setType(cosmetic.type().name());
    entity.setImageUrl(cosmetic.imageUrl());
    entity.setActive(cosmetic.active());
    return toDomain(persistenceRepository.save(entity));
  }

  @Override
  public Optional<Cosmetic> findById(UUID id) {
    return persistenceRepository.findById(id.toString()).map(CosmeticRepositoryImpl::toDomain);
  }

  @Override
  public List<Cosmetic> findActive() {
    return persistenceRepository.findAllByActiveTrueOrderByNameAsc().stream()
        .map(CosmeticRepositoryImpl::toDomain)
        .toList();
  }

  private static Cosmetic toDomain(CosmeticPersistenceEntity entity) {
    return new Cosmetic(
        UUID.fromString(entity.getId()),
        entity.getName(),
        entity.getDescription(),
        entity.getPriceInGems(),
        CosmeticType.valueOf(entity.getType()),
        entity.getImageUrl(),
        entity.isActive());
  }
}
