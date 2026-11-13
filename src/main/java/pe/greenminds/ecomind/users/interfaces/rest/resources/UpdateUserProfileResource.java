package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Name and progress values of a profile")
public record UpdateUserProfileResource(
    @Schema(description = "Name shown in the profile", example = "Camila Torres")
    @NotBlank @Size(min = 2, max = 120)
    String name,
    @Schema(example = "6")
    @NotNull @Min(0)
    Integer streak,
    @Schema(example = "2026-10-08", nullable = true)
    LocalDate lastStreakDate,
    @Schema(example = "350")
    @NotNull @Min(0)
    Integer ecopoints,
    @Schema(example = "15")
    @NotNull @Min(0)
    Integer gemBalance) {
}
