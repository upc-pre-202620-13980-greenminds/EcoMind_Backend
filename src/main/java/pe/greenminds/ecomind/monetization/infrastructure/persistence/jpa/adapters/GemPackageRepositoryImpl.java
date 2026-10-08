package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemPackage;
import pe.greenminds.ecomind.monetization.domain.repositories.GemPackageRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPackagePersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.GemPackagePersistenceRepository;

@Repository
public class GemPackageRepositoryImpl implements GemPackageRepository {
  private final GemPackagePersistenceRepository persistenceRepository;
  public GemPackageRepositoryImpl(GemPackagePersistenceRepository persistenceRepository) { this.persistenceRepository = persistenceRepository; }

  public GemPackage save(GemPackage gemPackage) {
    var id = gemPackage.id() == null ? UUID.randomUUID() : gemPackage.id();
    var entity = gemPackage.id() == null ? new GemPackagePersistenceEntity()
        : persistenceRepository.findById(id.toString()).orElseGet(GemPackagePersistenceEntity::new);
    entity.setId(id.toString()); entity.setName(gemPackage.name()); entity.setGemAmount(gemPackage.gemAmount());
    entity.setPrice(gemPackage.price()); entity.setCurrency(gemPackage.currency()); entity.setActive(gemPackage.active());
    return toDomain(persistenceRepository.save(entity));
  }
  public Optional<GemPackage> findById(UUID id) { return persistenceRepository.findById(id.toString()).map(GemPackageRepositoryImpl::toDomain); }
  public List<GemPackage> findActive() { return persistenceRepository.findAllByActiveTrueOrderByNameAsc().stream().map(GemPackageRepositoryImpl::toDomain).toList(); }
  private static GemPackage toDomain(GemPackagePersistenceEntity e) { return new GemPackage(UUID.fromString(e.getId()), e.getName(), e.getGemAmount(), e.getPrice(), e.getCurrency(), e.isActive()); }
}
