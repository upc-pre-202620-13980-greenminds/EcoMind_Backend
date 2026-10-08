package pe.greenminds.ecomind.quests.domain.model.commands;

public record CompleteFamilyPlanCommand(
        Long familyPlanId,
        Long ownerUserId
) {
}
