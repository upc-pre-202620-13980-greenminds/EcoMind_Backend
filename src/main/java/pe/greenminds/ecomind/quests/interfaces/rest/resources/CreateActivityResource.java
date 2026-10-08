package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;

@Schema(
        name = "CreateActivityRequest",
        description = "Data required to create an activity",
        example = """
        {
              "questId": 1,
              "description": "Check the rooms",
              "order": 1,
              "type": "CHECKBOX",
              "activityConfiguration": null,
              "image": null
        }
        """
)
public record CreateActivityResource(
        @NotNull
        @Schema(description = "Quest where the activity exists")
        Long questId,

        @Schema(description = "Activity instructions")
        String description,

        @NotNull
        @Positive
        @Schema(description = "Number of order of the activity")
        Integer order,

        @NotNull
        @Schema(description = "Type of activity")
        ActivityType type,

        @Schema(
                description = "Configuration for the activity type",
                example = "{}",
                nullable = true
        )
        Map<String, Object> activityConfiguration,

        @Schema(description = "Image url for individual activity")
        String image
) {
}
