package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
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
    var entity = gemPackage.id() == null ? new GemPackagePersistenceEntity()
        : persistenceRepository.findById(gemPackage.id()).orElseGet(GemPackagePersistenceEntity::new);
    entity.setId(gemPackage.id()); entity.setName(gemPackage.name()); entity.setGemAmount(gemPackage.gemAmount());
    entity.setPrice(gemPackage.price()); entity.setCurrency(gemPackage.currency()); entity.setActive(gemPackage.active());
    return toDomain(persistenceRepository.save(entity));
  }
  public Optional<GemPackage> findById(Long id) { return persistenceRepository.findById(id).map(GemPackageRepositoryImpl::toDomain); }
  public List<GemPackage> findActive() { return persistenceRepository.findAllByActiveTrueOrderByNameAsc().stream().map(GemPackageRepositoryImpl::toDomain).toList(); }
  private static GemPackage toDomain(GemPackagePersistenceEntity e) { return new GemPackage(e.getId(), e.getName(), e.getGemAmount(), e.getPrice(), e.getCurrency(), e.isActive()); }
}
