package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Answer to a friend request")
public record RespondFriendRequestResource(
    @Schema(description = "true to accept the request, false to reject it", example = "true")
    @NotNull
    Boolean accepted) {
}
