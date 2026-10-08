package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostReactionResource(
        @Schema(description = "Post identifier", example = "1") @NotNull Long post_id,
        @Schema(description = "Reaction assigned to the post", example = "like",
                allowableValues = {"like", "funny", "love", "surprise", "sad", "angry"})
        @NotBlank String reaction_type) {}
