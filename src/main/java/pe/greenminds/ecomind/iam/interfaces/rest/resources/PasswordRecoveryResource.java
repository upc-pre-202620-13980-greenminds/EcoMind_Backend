package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email address of the account whose password must be recovered")
public record PasswordRecoveryResource(
    @Schema(example = "camila.torres@example.com")
    @NotBlank @Email
    String email) {
}
