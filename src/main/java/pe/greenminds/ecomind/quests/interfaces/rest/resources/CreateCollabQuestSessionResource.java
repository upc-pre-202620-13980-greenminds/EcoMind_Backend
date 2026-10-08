package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "CreateCollabQuestSessionRequest",
        description = "Request payload for creating a collaborative quest session.",
        example = """
        {
          "questId": 18
        }
        """
)
public record CreateCollabQuestSessionResource(
        @NotNull
        @Positive
        @Schema(description = "Quest identifier", example = "18")
        Long questId
) {
}
