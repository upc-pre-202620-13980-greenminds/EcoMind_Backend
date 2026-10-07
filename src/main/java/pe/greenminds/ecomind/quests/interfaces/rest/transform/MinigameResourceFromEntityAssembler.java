package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.MinigameResource;

public final class MinigameResourceFromEntityAssembler {
    private MinigameResourceFromEntityAssembler() {
    }

    public static MinigameResource toResourceFromEntity(Minigame minigame) {
        return new MinigameResource(
                minigame.getId(),
                minigame.getName(),
                minigame.getDescription(),
                minigame.getUrl(),
                minigame.getCompletionRules()
        );
    }
}
