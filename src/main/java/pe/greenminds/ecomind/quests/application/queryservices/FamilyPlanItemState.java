package pe.greenminds.ecomind.quests.application.queryservices;

public record FamilyPlanItemState(
        Long id,
        Long questId,
        Long collaborativeSessionId,
        Double progress
) {
}
