package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
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
    var entity = multiplier.id() == null ? new MultiplierPersistenceEntity()
        : persistenceRepository.findById(multiplier.id()).orElseGet(MultiplierPersistenceEntity::new);
    entity.setId(multiplier.id()); entity.setName(multiplier.name()); entity.setDescription(multiplier.description());
    entity.setFactor(multiplier.factor()); entity.setDurationMinutes(multiplier.durationMinutes());
    entity.setPriceInGems(multiplier.priceInGems()); entity.setActive(multiplier.active());
    return toDomain(persistenceRepository.save(entity));
  }
  public Optional<Multiplier> findById(Long id) { return persistenceRepository.findById(id).map(MultiplierRepositoryImpl::toDomain); }
  public List<Multiplier> findActive() { return persistenceRepository.findAllByActiveTrueOrderByNameAsc().stream().map(MultiplierRepositoryImpl::toDomain).toList(); }
  private static Multiplier toDomain(MultiplierPersistenceEntity e) { return new Multiplier(e.getId(), e.getName(), e.getDescription(), e.getFactor(), e.getDurationMinutes(), e.getPriceInGems(), e.isActive()); }
}
