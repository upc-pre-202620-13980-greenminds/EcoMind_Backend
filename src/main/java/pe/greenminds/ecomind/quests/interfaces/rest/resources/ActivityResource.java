package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;

@Schema(
        name="ActivityResponse",
        description = "Activity information response",
        example = """
        {
          "id": 1,
          "questId": 1,
          "description": "Check the rooms",
          "order": 1,
          "type": "CHECKBOX",
          "activityConfiguration": null,
          "image": null
        }
        """
)
public record ActivityResource(
        @Schema(description = "Activity unique identifier")
        Long id,

        @Schema(description = "Quest where the activity exists", example = "1")
        Long questId,

        @Schema(description = "Activity instructions", example = "Check the rooms")
        String description,

        @Schema(description = "Number of order of the activity", example = "1")
        Integer order,

        @Schema(description = "Type of activity", example = "CHECKBOX")
        ActivityType type,

        @Schema(description = "Configuration for the activity type", nullable = true)
        Map<String, Object> activityConfiguration,

        @Schema(description = "Image url for individual activity", example = "null")
        String image
) {
}
