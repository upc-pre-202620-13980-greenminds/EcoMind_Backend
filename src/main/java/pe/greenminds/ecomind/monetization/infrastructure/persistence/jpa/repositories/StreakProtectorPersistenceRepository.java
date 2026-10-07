package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.StreakProtectorPersistenceEntity;

public interface StreakProtectorPersistenceRepository extends JpaRepository<StreakProtectorPersistenceEntity, Long> {
  List<StreakProtectorPersistenceEntity> findAllByActiveTrueOrderByNameAsc();
}
