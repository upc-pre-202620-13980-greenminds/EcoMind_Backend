package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.monetization.domain.model.entities.Multiplier;
import pe.greenminds.ecomind.monetization.domain.repositories.MultiplierRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.MultiplierPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.MultiplierPersistenceRepository;

@Repository
public class MultiplierRepositoryImpl implements MultiplierRepository {
  private final MultiplierPersistenceRepository persistenceRepository;
  public MultiplierRepositoryImpl(MultiplierPersistenceRepository persistenceRepository) { this.persistenceRepository = persistenceRepository; }

  public Multiplier save(Multiplier multiplier) {
    var id = multiplier.id() == null ? UUID.randomUUID() : multiplier.id();
    var entity = multiplier.id() == null ? new MultiplierPersistenceEntity()
        : persistenceRepository.findById(id.toString()).orElseGet(MultiplierPersistenceEntity::new);
    entity.setId(id.toString()); entity.setName(multiplier.name()); entity.setDescription(multiplier.description());
    entity.setFactor(multiplier.factor()); entity.setDurationMinutes(multiplier.durationMinutes());
    entity.setPriceInGems(multiplier.priceInGems()); entity.setActive(multiplier.active());
    return toDomain(persistenceRepository.save(entity));
  }
  public Optional<Multiplier> findById(UUID id) { return persistenceRepository.findById(id.toString()).map(MultiplierRepositoryImpl::toDomain); }
  public List<Multiplier> findActive() { return persistenceRepository.findAllByActiveTrueOrderByNameAsc().stream().map(MultiplierRepositoryImpl::toDomain).toList(); }
  private static Multiplier toDomain(MultiplierPersistenceEntity e) { return new Multiplier(UUID.fromString(e.getId()), e.getName(), e.getDescription(), e.getFactor(), e.getDurationMinutes(), e.getPriceInGems(), e.isActive()); }
}
