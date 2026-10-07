package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "CreateMinigameAttemptRequest",
        example = """
        {
          "userId": 3,
          "questId": 10
        }
        """
)
public record CreateMinigameAttemptResource(
        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long questId
) {
}
