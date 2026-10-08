package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "InviteCollabQuestMemberRequest",
        description = "Request payload for inviting a user to a collaborative quest session.",
        example = """
        {
          "sessionId": 1,
          "invitedByUserId": 1,
          "invitedUserId": 3
        }
        """
)
public record InviteCollabQuestMemberResource(
        @NotNull
        @Positive
        @Schema(description = "Collaborative session identifier", example = "1")
        Long sessionId,

        @NotNull
        @Positive
        @Schema(description = "User identifier that sends the invitation", example = "1")
        Long invitedByUserId,

        @NotNull
        @Positive
        @Schema(description = "Invited user identifier", example = "3")
        Long invitedUserId
) {
}
