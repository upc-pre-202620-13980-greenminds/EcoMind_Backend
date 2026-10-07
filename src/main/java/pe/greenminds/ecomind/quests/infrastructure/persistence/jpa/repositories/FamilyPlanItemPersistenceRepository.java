package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.FamilyPlanItemPersistenceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyPlanItemPersistenceRepository
        extends JpaRepository<FamilyPlanItemPersistenceEntity, Long> {
    List<FamilyPlanItemPersistenceEntity> findByFamilyPlanId(Long familyPlanId);
    Optional<FamilyPlanItemPersistenceEntity> findByCollaborativeSessionId(
            Long collaborativeSessionId
    );
    boolean existsByCollaborativeSessionId(Long collaborativeSessionId);
    void deleteByFamilyPlanId(Long familyPlanId);
}
