package pe.greenminds.ecomind.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identity of an account")
public record AuthenticatedUserResource(
    @Schema(description = "Id of the account", example = "1")
    Long accountId,
    @Schema(example = "camila.torres@example.com")
    String email) {
}
