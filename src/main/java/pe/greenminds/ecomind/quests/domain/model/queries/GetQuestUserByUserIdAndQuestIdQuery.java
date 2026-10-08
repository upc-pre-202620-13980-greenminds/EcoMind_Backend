package pe.greenminds.ecomind.quests.domain.model.queries;

public record GetQuestUserByUserIdAndQuestIdQuery(
        Long userId,
        Long questId
) {
}
