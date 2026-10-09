package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.CreateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.FamilyPlanItemCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateFamilyPlanResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.FamilyPlanItemRequestResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.UpdateFamilyPlanResource;

import java.util.List;

public final class FamilyPlanCommandFromResourceAssembler {
    private FamilyPlanCommandFromResourceAssembler() {
    }

    public static CreateFamilyPlanCommand toCommandFromResource(
            CreateFamilyPlanResource resource,
            Long authenticatedUserId
    ) {
        return new CreateFamilyPlanCommand(
                resource.familyId(),
                authenticatedUserId,
                safeItems(resource.items()).stream()
                        .map(FamilyPlanCommandFromResourceAssembler::toItemCommand)
                        .toList()
        );
    }

    public static UpdateFamilyPlanCommand toCommandFromResource(
            Long familyPlanId,
            UpdateFamilyPlanResource resource
    ) {
        return new UpdateFamilyPlanCommand(
                familyPlanId,
                safeItems(resource.items()).stream()
                        .map(FamilyPlanCommandFromResourceAssembler::toItemCommand)
                        .toList()
        );
    }

    private static List<FamilyPlanItemRequestResource> safeItems(
            List<FamilyPlanItemRequestResource> items
    ) {
        return items == null ? List.of() : items;
    }

    private static FamilyPlanItemCommand toItemCommand(
            FamilyPlanItemRequestResource resource
    ) {
        return new FamilyPlanItemCommand(resource.questId());
    }
}
