package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;

@Schema(
        name = "UpdateActivityRequest",
        description = "Complete activity update payload"
)
public record UpdateActivityResource(
        String description,

        @NotNull
        @Positive
        Integer order,

        @NotNull
        @Schema(description = "Current activity type; changing it is not allowed")
        ActivityType type,

        @Schema(
                description = "Configuration for the activity type",
                example = "{}",
                nullable = true
        )
        Map<String, Object> activityConfiguration,

        String image
) {
}
