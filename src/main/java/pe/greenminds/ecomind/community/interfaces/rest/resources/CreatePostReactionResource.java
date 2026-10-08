package pe.greenminds.ecomind.community.interfaces.rest.resources;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostReactionResource(@NotNull Long post_id, @NotBlank String reaction_type) {}
