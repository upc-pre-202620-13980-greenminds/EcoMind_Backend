package pe.greenminds.ecomind.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Number of notifications the user has not read")
public record UnreadCountResource(
    @Schema(example = "3")
    long count) {
}
