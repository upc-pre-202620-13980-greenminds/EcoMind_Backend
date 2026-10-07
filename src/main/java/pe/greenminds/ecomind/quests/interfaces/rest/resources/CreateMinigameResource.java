package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(
        name = "CreateMinigameRequest",
        example = """
        {
          "name": "Recolector Eco",
          "description": "Suma puntos recolectando energia hasta alcanzar la meta.",
          "url": "/quests/minigames/simple-score",
          "completionRules": {
            "minScore": 800
          }
        }
        """
)
public record CreateMinigameResource(
        @NotBlank
        String name,

        String description,

        @NotBlank
        String url,

        @NotNull
        Map<String, Object> completionRules
) {
}
