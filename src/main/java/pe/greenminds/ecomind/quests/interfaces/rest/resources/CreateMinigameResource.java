package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(
        name = "CreateMinigameRequest",
        example = """
        {
          "name": "Recoge residuos",
          "description": "Recoge residuos hasta alcanzar el puntaje objetivo.",
          "url": "/games/collect-waste",
          "completionRules": {
            "minScore": 10
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
