package pe.greenminds.ecomind.quests.domain.model.commands;

public record CreateActivityUserCommand(
        Long questUserId,
        Long activityId,
        Long collaborativeSessionId
) {
}
