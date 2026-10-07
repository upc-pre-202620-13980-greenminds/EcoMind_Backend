package pe.greenminds.ecomind.quests.domain.model.commands;

public record CreateQuestUserCommand(
        Long userId,
        Long questId,
        Long collaborativeSessionId
) {
}
