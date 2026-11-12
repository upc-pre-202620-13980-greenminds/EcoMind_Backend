package pe.greenminds.ecomind.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Notification of the inbox, with its texts in the language of the request")
public record NotificationResource(
    @Schema(example = "15")
    Long id,
    @Schema(example = "COMMUNITY_ACTIVITY")
    String category,
    @Schema(example = "FRIEND_REQUEST_ACCEPTED", allowableValues = {"GENERAL",
        "FRIEND_REQUEST_ACCEPTED"})
    String type,
    @Schema(example = "Friend request accepted")
    String title,
    @Schema(example = "Ana Torres accepted your friend request.")
    String message,
    @Schema(example = "false")
    boolean read,
    @Schema(example = "2026-11-11T15:30:00Z")
    Instant createdAt) {
}
