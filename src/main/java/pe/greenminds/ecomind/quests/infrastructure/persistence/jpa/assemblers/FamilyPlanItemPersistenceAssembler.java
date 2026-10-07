package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.FamilyPlanItemPersistenceEntity;

public final class FamilyPlanItemPersistenceAssembler {
    private FamilyPlanItemPersistenceAssembler() {
    }

    public static FamilyPlanItem toDomainFromPersistence(FamilyPlanItemPersistenceEntity entity) {
        return new FamilyPlanItem(
                entity.getId(),
                entity.getFamilyPlanId(),
                entity.getQuestId(),
                entity.getCollaborativeSessionId()
        );
    }

    public static FamilyPlanItemPersistenceEntity toPersistenceFromDomain(
            FamilyPlanItem familyPlanItem
    ) {
        var entity = new FamilyPlanItemPersistenceEntity();
        entity.setId(familyPlanItem.getId());
        entity.setFamilyPlanId(familyPlanItem.getFamilyPlanId());
        entity.setQuestId(familyPlanItem.getQuestId());
        entity.setCollaborativeSessionId(familyPlanItem.getCollaborativeSessionId());
        return entity;
    }
}
