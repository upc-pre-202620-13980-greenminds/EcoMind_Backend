package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "CreateActivityUserRequest",
        description = "Request payload for assigning an activity to a quest user",
        example = """
        {
          "questUserId": 1,
          "activityId": 1,
          "collaborativeSessionId": null
        }
        """
)
public record CreateActivityUserResource(
        @NotNull
        @Positive
        Long questUserId,

        @NotNull
        @Positive
        Long activityId,

        @Positive
        Long collaborativeSessionId
) {
}
