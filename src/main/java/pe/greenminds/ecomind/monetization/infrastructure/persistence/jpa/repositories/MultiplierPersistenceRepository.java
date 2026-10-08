package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.MultiplierPersistenceEntity;

public interface MultiplierPersistenceRepository extends JpaRepository<MultiplierPersistenceEntity, String> {
  List<MultiplierPersistenceEntity> findAllByActiveTrueOrderByNameAsc();
}
