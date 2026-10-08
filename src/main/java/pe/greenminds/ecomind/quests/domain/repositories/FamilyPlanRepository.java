package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlan;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;

import java.util.List;
import java.util.Optional;

public interface FamilyPlanRepository {
    FamilyPlan save(FamilyPlan familyPlan);
    Optional<FamilyPlan> findById(Long id);
    List<FamilyPlan> findByFamilyId(Long familyId);
    Optional<FamilyPlan> findByFamilyIdAndStatus(Long familyId, FamilyPlanStatus status);
    void deleteById(Long id);
}
