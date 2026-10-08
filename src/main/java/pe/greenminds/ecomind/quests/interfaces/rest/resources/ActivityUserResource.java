package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.Map;

public record ActivityUserResource(
        Long id,
        Long questUserId,
        Long activityId,
        Double progress,
        LocalDate endDate,
        String activityDescription,
        Map<String, Object> activityConfiguration,
        Long collaborativeSessionId
) {
}
