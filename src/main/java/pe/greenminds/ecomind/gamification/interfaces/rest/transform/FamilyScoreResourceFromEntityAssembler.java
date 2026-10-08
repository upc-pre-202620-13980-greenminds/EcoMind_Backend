package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.FamilyScoreResource;

public final class FamilyScoreResourceFromEntityAssembler {
    private FamilyScoreResourceFromEntityAssembler() {}

    public static FamilyScoreResource toResourceFromEntity(FamilyScore entity) {
        return new FamilyScoreResource(entity.getFamilyId().value(), entity.getTotalEcopoints());
    }
}
