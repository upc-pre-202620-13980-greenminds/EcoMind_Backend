package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Recovery token received by email and the new password")
public record ConfirmPasswordRecoveryResource(
    @Schema(description = "Token included in the recovery link",
        example = "q8Zr1mF0sVJ3o2cXbT7yLk5nA9dEwHuG4iPpQeRtYsU")
    @NotBlank
    String token,
    @Schema(description = "8 to 72 characters with at least one letter and one digit",
        example = "NewGreenPlanet2026")
    @NotBlank @Size(max = 72)
    String newPassword) {
}
