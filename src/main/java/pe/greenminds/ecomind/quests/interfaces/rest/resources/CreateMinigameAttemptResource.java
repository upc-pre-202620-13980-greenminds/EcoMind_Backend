package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "CreateMinigameAttemptRequest",
        example = """
        {
          "questId": 10
        }
        """
)
public record CreateMinigameAttemptResource(
        @NotNull
        @Positive
        Long questId
) {
}
