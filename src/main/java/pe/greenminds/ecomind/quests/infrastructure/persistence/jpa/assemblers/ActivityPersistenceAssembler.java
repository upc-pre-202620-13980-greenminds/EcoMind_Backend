package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.ActivityPersistenceEntity;

public class ActivityPersistenceAssembler {
    private ActivityPersistenceAssembler(){}

    public static Activity toDomainFromPersistence(ActivityPersistenceEntity entity) {
        return new Activity(
                entity.getId(),
                entity.getQuestId(),
                entity.getDescription(),
                entity.getOrder(),
                entity.getType(),
                entity.getActivityConfiguration(),
                entity.getImage()
        );
    }

    public static ActivityPersistenceEntity toPersistenceFromDomain(Activity activity) {
        var entity = new ActivityPersistenceEntity();
        entity.setId(activity.getId());
        entity.setQuestId(activity.getQuestId());
        entity.setDescription(activity.getDescription());
        entity.setOrder(activity.getOrder());
        entity.setType(activity.getActivityType());
        entity.setActivityConfiguration(activity.getActivityConfiguration());
        entity.setImage(activity.getImage());
        return entity;
    }
}
