package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Registration waiting for the email to be verified")
public record PendingRegistrationResource(
    @Schema(description = "Normalized email address the code was sent to",
        example = "camila.torres@example.com")
    String email,
    @Schema(description = "Moment the verification code stops being valid",
        example = "2026-10-07T15:20:00Z")
    Instant expiresAt) {
}
