package pe.greenminds.ecomind.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Notification for the authenticated user, such as a reminder")
public record CreateNotificationResource(
    @Schema(example = "LEARNING_REMINDERS")
    @NotBlank
    @Pattern(
        regexp = "NEW_AND_DAILY_QUESTS|LEARNING_REMINDERS|ACHIEVEMENTS_AND_BADGES"
            + "|WEEKLY_PROGRESS_SUMMARY|COMMUNITY_EVENTS|COMMUNITY_ACTIVITY",
        message = "{notifications.validation.category}")
    String category,
    @Schema(example = "Pending material: Recycling essentials")
    @NotBlank @Size(max = 120)
    String title,
    @Schema(example = "2 days left to review it.")
    @NotBlank @Size(max = 500)
    String message) {
}
