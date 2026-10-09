package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.CreateMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CreateMinigameAttemptResource;

public final class CreateMinigameAttemptCommandFromResourceAssembler {
    private CreateMinigameAttemptCommandFromResourceAssembler() {
    }

    public static CreateMinigameAttemptCommand toCommandFromResource(
            CreateMinigameAttemptResource resource,
            Long authenticatedUserId
    ) {
        return new CreateMinigameAttemptCommand(authenticatedUserId, resource.questId());
    }
}
