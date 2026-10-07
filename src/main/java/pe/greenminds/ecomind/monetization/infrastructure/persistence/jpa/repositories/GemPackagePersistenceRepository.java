package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPackagePersistenceEntity;

public interface GemPackagePersistenceRepository extends JpaRepository<GemPackagePersistenceEntity, Long> {
  List<GemPackagePersistenceEntity> findAllByActiveTrueOrderByNameAsc();
}
