package pe.greenminds.ecomind.shared.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body for operations that only need to confirm what happened.
 */
@Schema(description = "Informative message about the result of an operation")
public record MessageResource(
    @Schema(
        description = "Message in the language requested with Accept-Language",
        example = "If the email address is registered, a recovery link has been sent.")
    String message) {
}
