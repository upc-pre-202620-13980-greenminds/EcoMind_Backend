package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Proof that the user controls the email address")
public record VerifyEmailResource(
    @Schema(description = "Email address used in the registration",
        example = "camila.torres@example.com")
    @NotBlank @Email
    String email,
    @Schema(description = "Six-digit code received by email", example = "482913")
    @NotBlank @Pattern(regexp = "\\d{6}", message = "{iam.validation.verification-code}")
    String code) {
}
