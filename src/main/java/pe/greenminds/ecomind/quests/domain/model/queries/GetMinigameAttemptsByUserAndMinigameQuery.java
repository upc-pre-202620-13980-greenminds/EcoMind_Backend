package pe.greenminds.ecomind.quests.domain.model.queries;

public record GetMinigameAttemptsByUserAndMinigameQuery(
        Long userId,
        Long minigameId
) {
}
