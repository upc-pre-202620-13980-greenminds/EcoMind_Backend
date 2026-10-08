package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanItemState;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanState;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.FamilyPlanItemResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.FamilyPlanResource;

public final class FamilyPlanResourceFromStateAssembler {
    private FamilyPlanResourceFromStateAssembler() {
    }

    public static FamilyPlanResource toResourceFromState(FamilyPlanState state) {
        return new FamilyPlanResource(
                state.id(),
                state.familyId(),
                state.ownerUserId(),
                state.status(),
                state.progress(),
                state.items().stream()
                        .map(FamilyPlanResourceFromStateAssembler::toItemResource)
                        .toList()
        );
    }

    private static FamilyPlanItemResource toItemResource(FamilyPlanItemState state) {
        return new FamilyPlanItemResource(
                state.id(),
                state.questId(),
                state.collaborativeSessionId(),
                state.progress()
        );
    }
}
