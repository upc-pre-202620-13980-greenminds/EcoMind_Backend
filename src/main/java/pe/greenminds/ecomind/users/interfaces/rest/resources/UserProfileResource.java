package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Profile of a user")
public record UserProfileResource(
    @Schema(description = "Id of the user, the same as the account id", example = "1")
    Long id,
    @Schema(example = "Camila Torres")
    String name,
    @Schema(example = "STUDENT", allowableValues = {"STUDENT", "PARENT"})
    String socialRole,
    @Schema(description = "Consecutive days with activity", example = "5")
    int streak,
    @Schema(description = "Last day that counted for the streak", example = "2026-10-07")
    LocalDate lastStreakDate,
    @Schema(example = "320")
    int ecopoints,
    @Schema(example = "15")
    int gemBalance,
    @Schema(description = "Cosmetic shown in the profile, if any", example = "4", nullable = true)
    Long equippedCosmeticId) {
}
