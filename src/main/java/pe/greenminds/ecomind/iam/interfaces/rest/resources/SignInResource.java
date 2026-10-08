package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials of an account")
public record SignInResource(
    @Schema(example = "camila.torres@example.com")
    @NotBlank
    String email,
    @Schema(example = "GreenPlanet2026")
    @NotBlank
    String password) {
}
