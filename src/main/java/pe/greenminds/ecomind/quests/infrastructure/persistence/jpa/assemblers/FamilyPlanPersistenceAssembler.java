package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlan;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.FamilyPlanPersistenceEntity;

public final class FamilyPlanPersistenceAssembler {
    private FamilyPlanPersistenceAssembler() {
    }

    public static FamilyPlan toDomainFromPersistence(FamilyPlanPersistenceEntity entity) {
        return new FamilyPlan(
                entity.getId(),
                entity.getFamilyId(),
                entity.getOwnerUserId(),
                entity.getStatus(),
                entity.getCompletedAt()
        );
    }

    public static FamilyPlanPersistenceEntity toPersistenceFromDomain(FamilyPlan familyPlan) {
        var entity = new FamilyPlanPersistenceEntity();
        entity.setId(familyPlan.getId());
        entity.setFamilyId(familyPlan.getFamilyId());
        entity.setOwnerUserId(familyPlan.getOwnerUserId());
        entity.setStatus(familyPlan.getStatus());
        entity.setCompletedAt(familyPlan.getCompletedAt());
        return entity;
    }
}
