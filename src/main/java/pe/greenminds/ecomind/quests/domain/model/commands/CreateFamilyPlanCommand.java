package pe.greenminds.ecomind.quests.domain.model.commands;

import java.util.List;

public record CreateFamilyPlanCommand(
        Long familyId,
        Long ownerUserId,
        List<FamilyPlanItemCommand> items
) {
}
