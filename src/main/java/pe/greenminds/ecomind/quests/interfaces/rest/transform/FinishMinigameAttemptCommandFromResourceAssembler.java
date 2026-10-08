package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.commands.FinishMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.FinishMinigameAttemptResource;

public final class FinishMinigameAttemptCommandFromResourceAssembler {
    private FinishMinigameAttemptCommandFromResourceAssembler() {
    }

    public static FinishMinigameAttemptCommand toCommandFromResource(
            Long attemptId,
            FinishMinigameAttemptResource resource
    ) {
        return new FinishMinigameAttemptCommand(
                attemptId,
                resource.score(),
                resource.metadata()
        );
    }
}
