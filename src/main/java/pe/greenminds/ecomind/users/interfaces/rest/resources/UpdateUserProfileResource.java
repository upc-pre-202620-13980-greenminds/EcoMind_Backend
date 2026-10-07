package pe.greenminds.ecomind.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Progress values of a profile")
public record UpdateUserProfileResource(
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
