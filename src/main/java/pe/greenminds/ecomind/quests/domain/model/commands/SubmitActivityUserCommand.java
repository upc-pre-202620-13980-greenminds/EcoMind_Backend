package pe.greenminds.ecomind.quests.domain.model.commands;

import java.util.Map;

public record SubmitActivityUserCommand(
        Long activityUserId,
        Map<String, Object> data
) {
}
