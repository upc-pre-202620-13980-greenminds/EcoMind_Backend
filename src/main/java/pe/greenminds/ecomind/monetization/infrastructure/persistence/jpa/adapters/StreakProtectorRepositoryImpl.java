package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.monetization.domain.model.entities.StreakProtector;
import pe.greenminds.ecomind.monetization.domain.repositories.StreakProtectorRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.StreakProtectorPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.StreakProtectorPersistenceRepository;

@Repository
public class StreakProtectorRepositoryImpl implements StreakProtectorRepository {
  private final StreakProtectorPersistenceRepository persistenceRepository;
  public StreakProtectorRepositoryImpl(StreakProtectorPersistenceRepository persistenceRepository) { this.persistenceRepository = persistenceRepository; }

  public StreakProtector save(StreakProtector protector) {
    var entity = protector.id() == null ? new StreakProtectorPersistenceEntity()
        : persistenceRepository.findById(protector.id()).orElseGet(StreakProtectorPersistenceEntity::new);
    entity.setId(protector.id()); entity.setName(protector.name()); entity.setDescription(protector.description());
    entity.setPriceInGems(protector.priceInGems()); entity.setActive(protector.active());
    return toDomain(persistenceRepository.save(entity));
  }
  public Optional<StreakProtector> findById(Long id) { return persistenceRepository.findById(id).map(StreakProtectorRepositoryImpl::toDomain); }
  public List<StreakProtector> findActive() { return persistenceRepository.findAllByActiveTrueOrderByNameAsc().stream().map(StreakProtectorRepositoryImpl::toDomain).toList(); }
  private static StreakProtector toDomain(StreakProtectorPersistenceEntity e) { return new StreakProtector(e.getId(), e.getName(), e.getDescription(), e.getPriceInGems(), e.isActive()); }
}
