package pe.greenminds.ecomind.quests.domain.model.events;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

public record ActivityCreatedEvent(
        Long activityId,
        Long questId,
        String description,
        Integer order,
        ActivityType type,
        String image
) {
    public static   ActivityCreatedEvent from(Activity activity) {
        return new ActivityCreatedEvent(
                activity.getId(),
                activity.getQuestId(),
                activity.getDescription(),
                activity.getOrder(),
                activity.getActivityType(),
                activity.getImage()
        );
    }
}
