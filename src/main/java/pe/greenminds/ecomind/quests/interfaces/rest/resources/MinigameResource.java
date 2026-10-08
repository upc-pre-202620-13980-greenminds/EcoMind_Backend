package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import java.util.Map;

public record MinigameResource(
        Long id,
        String name,
        String description,
        String url,
        Map<String, Object> completionRules
) {
}
