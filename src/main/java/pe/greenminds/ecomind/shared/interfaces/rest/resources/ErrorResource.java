package pe.greenminds.ecomind.shared.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Error body returned by every endpoint when a request fails.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error returned when a request cannot be completed")
public record ErrorResource(
    @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
    String code,
    @Schema(
        description = "Message in the language requested with Accept-Language",
        example = "The request contains invalid data.")
    String message,
    @Schema(description = "Additional context, when available", example = "email: must not be blank")
    String details) {
}
