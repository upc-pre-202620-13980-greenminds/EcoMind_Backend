package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record CreateLocalCommunityResource(
        @NotBlank String name,
        String description,
        @NotBlank String locality,
        String icon_url) {}
