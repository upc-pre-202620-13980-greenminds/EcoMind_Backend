package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.EducationalMaterialEntity;

public interface EducationalMaterialRepository extends JpaRepository<EducationalMaterialEntity, Long> {
  List<EducationalMaterialEntity> findAllByActiveTrueOrderByTitleAsc();
  boolean existsByTitleIgnoreCaseAndLanguage(String title, String language);
}
