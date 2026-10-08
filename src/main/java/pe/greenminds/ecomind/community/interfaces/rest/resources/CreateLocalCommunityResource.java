package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLocalCommunityResource(
        @NotBlank String name,
        String description,
        @NotBlank String locality,
        String icon_url,
        @NotNull Long user_id) {}
