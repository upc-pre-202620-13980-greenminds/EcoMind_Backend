package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;

import java.util.List;

public record FamilyPlanState(
        Long id,
        Long familyId,
        Long ownerUserId,
        FamilyPlanStatus status,
        Double progress,
        List<FamilyPlanItemState> items
) {
}
