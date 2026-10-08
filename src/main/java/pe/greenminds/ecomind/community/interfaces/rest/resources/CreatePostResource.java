package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreatePostResource(
        @Schema(description = "Community identifier", example = "1") @NotNull Long community_id,
        @Schema(description = "Post text", example = "We completed a neighborhood energy-saving activity!")
        @NotBlank String content,
        @Schema(description = "Optional image URL", example = "https://example.com/activity.png", nullable = true)
        String image_url) {
}
