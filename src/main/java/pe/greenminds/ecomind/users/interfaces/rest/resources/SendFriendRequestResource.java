package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "User who will receive the friend request")
public record SendFriendRequestResource(
    @Schema(example = "8")
    @NotNull @Positive
    Long receiverId) {
}
