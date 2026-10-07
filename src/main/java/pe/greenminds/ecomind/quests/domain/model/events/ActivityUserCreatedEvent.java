package pe.greenminds.ecomind.quests.domain.model.events;

import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;

import java.time.LocalDate;

public record ActivityUserCreatedEvent(
        Long id,
        Long questUserId,
        Long activityId,
        Double progress,
        LocalDate endDate,
        Long collaborativeSessionId
) {
    public static ActivityUserCreatedEvent from(ActivityUser activityUser) {
        return new ActivityUserCreatedEvent(
                activityUser.getId(),
                activityUser.getQuestUserId(),
                activityUser.getActivityId(),
                activityUser.getProgress(),
                activityUser.getEndDate(),
                activityUser.getCollaborativeSessionId()
        );
    }
}
