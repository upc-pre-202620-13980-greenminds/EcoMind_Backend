package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostReactionTypeResource(@NotBlank String reaction_type) {}
