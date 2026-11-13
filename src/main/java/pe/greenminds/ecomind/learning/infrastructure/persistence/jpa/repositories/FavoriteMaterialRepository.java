package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.FavoriteMaterialEntity;

public interface FavoriteMaterialRepository extends JpaRepository<FavoriteMaterialEntity, Long> {
  Optional<FavoriteMaterialEntity> findByUserIdAndMaterialId(Long userId, Long materialId);
  List<FavoriteMaterialEntity> findAllByUserId(Long userId);
  void deleteAllByMaterialId(Long materialId);
}
