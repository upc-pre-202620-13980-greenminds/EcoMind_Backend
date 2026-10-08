package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.FamilyRewardResource;

public final class FamilyRewardResourceFromEntityAssembler {
    private FamilyRewardResourceFromEntityAssembler() {}

    public static FamilyRewardResource toResourceFromEntity(FamilyRewardTransaction entity) {
        return new FamilyRewardResource(
                entity.id(),
                "FAMILY_PLAN",
                entity.sourceExecutionId(),
                entity.familyId().value(),
                entity.ecopoints(),
                entity.occurredAt());
    }
}
