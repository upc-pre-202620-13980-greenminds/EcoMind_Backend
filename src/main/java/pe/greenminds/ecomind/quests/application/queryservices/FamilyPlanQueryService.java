package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.queries.GetActiveFamilyPlanByFamilyIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetFamilyPlanByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetFamilyPlansByFamilyIdQuery;

import java.util.List;
import java.util.Optional;

public interface FamilyPlanQueryService {
    Optional<FamilyPlanState> handle(GetFamilyPlanByIdQuery query);
    List<FamilyPlanState> handle(GetFamilyPlansByFamilyIdQuery query);
    Optional<FamilyPlanState> handle(GetActiveFamilyPlanByFamilyIdQuery query);
}
