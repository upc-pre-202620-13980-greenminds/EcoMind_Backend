package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem;

import java.util.List;
import java.util.Optional;

public interface FamilyPlanItemRepository {
    FamilyPlanItem save(FamilyPlanItem familyPlanItem);
    List<FamilyPlanItem> findByFamilyPlanId(Long familyPlanId);
    Optional<FamilyPlanItem> findByCollaborativeSessionId(Long collaborativeSessionId);
    boolean existsByCollaborativeSessionId(Long collaborativeSessionId);
    void deleteByFamilyPlanId(Long familyPlanId);
}
