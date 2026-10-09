package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.CreateCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateCollabQuestSessionResource;

public final class CreateCollabQuestSessionCommandFromResourceAssembler {
    private CreateCollabQuestSessionCommandFromResourceAssembler() {
    }

    public static CreateCollabQuestSessionCommand toCommandFromResource(
            CreateCollabQuestSessionResource resource,
            Long authenticatedUserId
    ) {
        return new CreateCollabQuestSessionCommand(
                resource.questId(),
                authenticatedUserId
        );
    }
}
