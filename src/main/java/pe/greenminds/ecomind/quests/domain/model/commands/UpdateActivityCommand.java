package pe.greenminds.ecomind.quests.domain.model.commands;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;

public record UpdateActivityCommand(
        Long activityId,
        String description,
        Integer order,
        ActivityType type,
        Map<String, Object> activityConfiguration,
        String image
) {
}
