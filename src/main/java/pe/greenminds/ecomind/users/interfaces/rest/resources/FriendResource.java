package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Friend request between two users and its state")
public record FriendResource(
    @Schema(example = "12")
    Long id,
    @Schema(description = "User who sent the request", example = "1")
    Long requesterId,
    @Schema(description = "User who received the request", example = "8")
    Long receiverId,
    @Schema(example = "PENDING", allowableValues = {"PENDING", "ACCEPTED", "REJECTED"})
    String status) {
}
