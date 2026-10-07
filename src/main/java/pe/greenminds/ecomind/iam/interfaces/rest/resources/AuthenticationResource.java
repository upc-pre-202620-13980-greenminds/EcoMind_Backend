package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Access token and identity of the account that signed in")
public record AuthenticationResource(
    @Schema(description = "JWT to send as Bearer token in protected requests",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.4pcPyMD09olPSyXnrXCjTwXyr4BsezdI1AVTmud2fU4")
    String accessToken,
    @Schema(description = "Moment the access token expires", example = "2026-10-07T16:00:00Z")
    Instant expiresAt,
    @Schema(description = "Id of the authenticated account", example = "1")
    Long accountId,
    @Schema(example = "camila.torres@example.com")
    String email) {
}
