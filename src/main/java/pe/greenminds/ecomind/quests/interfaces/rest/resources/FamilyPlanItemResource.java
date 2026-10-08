package pe.greenminds.ecomind.quests.interfaces.rest.resources;

public record FamilyPlanItemResource(
        Long id,
        Long questId,
        Long collaborativeSessionId,
        Double progress
) {
}
