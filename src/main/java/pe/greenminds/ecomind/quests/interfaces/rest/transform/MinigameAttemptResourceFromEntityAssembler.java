package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.MinigameAttemptResource;

public final class MinigameAttemptResourceFromEntityAssembler {
    private MinigameAttemptResourceFromEntityAssembler() {
    }

    public static MinigameAttemptResource toResourceFromEntity(MinigameAttempt attempt) {
        return new MinigameAttemptResource(
                attempt.getId(),
                attempt.getUserId(),
                attempt.getQuestId(),
                attempt.getScore(),
                attempt.getStatus(),
                attempt.getStartDate(),
                attempt.getEndDate(),
                attempt.getMetadata()
        );
    }
}
