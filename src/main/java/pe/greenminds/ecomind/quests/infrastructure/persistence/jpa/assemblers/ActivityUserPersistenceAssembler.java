package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.ActivityUserPersistenceEntity;

public final class ActivityUserPersistenceAssembler {
    private ActivityUserPersistenceAssembler() {
    }

    public static ActivityUser toDomainFromPersistence(ActivityUserPersistenceEntity entity) {
        return new ActivityUser(
                entity.getId(),
                entity.getQuestUserId(),
                entity.getActivityId(),
                entity.getProgress(),
                entity.getEndDate(),
                entity.getActivityDescription(),
                entity.getActivityConfiguration(),
                entity.getCollaborativeSessionId()
        );
    }

    public static ActivityUserPersistenceEntity toPersistenceFromDomain(ActivityUser activityUser) {
        var entity = new ActivityUserPersistenceEntity();
        entity.setId(activityUser.getId());
        entity.setQuestUserId(activityUser.getQuestUserId());
        entity.setActivityId(activityUser.getActivityId());
        entity.setProgress(activityUser.getProgress());
        entity.setEndDate(activityUser.getEndDate());
        entity.setActivityDescription(activityUser.getActivityDescription());
        entity.setActivityConfiguration(activityUser.getActivityConfiguration());
        entity.setCollaborativeSessionId(activityUser.getCollaborativeSessionId());
        return entity;
    }
}
