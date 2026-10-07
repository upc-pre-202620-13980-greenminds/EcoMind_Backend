package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.CosmeticPersistenceEntity;

public interface CosmeticPersistenceRepository
    extends JpaRepository<CosmeticPersistenceEntity, String> {
  List<CosmeticPersistenceEntity> findAllByActiveTrueOrderByNameAsc();
}
