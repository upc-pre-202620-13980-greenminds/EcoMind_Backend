package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdatePostReactionTypeResource(
        @Schema(description = "New reaction assigned to the post", example = "love",
                allowableValues = {"like", "funny", "love", "surprise", "sad", "angry"})
        @NotBlank String reaction_type) {}
