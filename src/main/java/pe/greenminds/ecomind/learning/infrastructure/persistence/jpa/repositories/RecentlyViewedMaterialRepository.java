package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.RecentlyViewedMaterialEntity;

public interface RecentlyViewedMaterialRepository extends JpaRepository<RecentlyViewedMaterialEntity, Long> {
  Optional<RecentlyViewedMaterialEntity> findByUserIdAndMaterialId(Long userId, Long materialId);
  List<RecentlyViewedMaterialEntity> findAllByUserIdOrderByViewedAtDesc(Long userId);
  void deleteAllByMaterialId(Long materialId);
}
