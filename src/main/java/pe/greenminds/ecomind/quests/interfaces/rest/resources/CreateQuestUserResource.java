package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "CreateQuestUserRequest",
        description = """
                Request payload for assigning a quest to a user.
                The quest activities are assigned automatically to the new quest user.
                """,
        example = """
        {
          "userId": 1,
          "questId": 1,
          "collaborativeSessionId": null
        }
        """
)
public record CreateQuestUserResource(
        @NotNull
        @Positive
        @Schema(description = "User identifier", example = "1")
        Long userId,

        @NotNull
        @Positive
        @Schema(description = "Quest identifier", example = "1")
        Long questId,

        @Positive
        @Schema(
                description = """
                        Optional collaborative session identifier propagated to the
                        automatically created activity assignments.
                        """,
                example = "1"
        )
        Long collaborativeSessionId
) {
}
