package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.FamilyPlanPersistenceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyPlanPersistenceRepository
        extends JpaRepository<FamilyPlanPersistenceEntity, Long> {
    List<FamilyPlanPersistenceEntity> findByFamilyId(Long familyId);
    Optional<FamilyPlanPersistenceEntity> findFirstByFamilyIdAndStatusOrderByIdDesc(
            Long familyId,
            FamilyPlanStatus status
    );
}
