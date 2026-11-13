package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.TutorialProgressEntity;

public interface TutorialProgressRepository extends JpaRepository<TutorialProgressEntity, Long> {
  Optional<TutorialProgressEntity> findByUserId(Long userId);
}
