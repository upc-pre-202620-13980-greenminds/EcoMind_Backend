package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to start a registration")
public record SubmitRegistrationResource(
    @Schema(description = "Name shown in the profile", example = "Camila Torres")
    @NotBlank @Size(min = 2, max = 120)
    String name,
    @Schema(description = "Email address that will identify the account",
        example = "camila.torres@example.com")
    @NotBlank @Email @Size(max = 255)
    String email,
    @Schema(description = "8 to 72 characters with at least one letter and one digit",
        example = "GreenPlanet2026")
    @NotBlank @Size(max = 72)
    String password,
    @Schema(description = "Role of the user in the platform", example = "STUDENT",
        allowableValues = {"STUDENT", "PARENT"})
    @NotBlank @Pattern(regexp = "STUDENT|PARENT", message = "{iam.validation.social-role}")
    String socialRole) {
}
