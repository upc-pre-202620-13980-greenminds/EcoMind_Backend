package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateLocalCommunityResource(
        @Schema(description = "Name of the local community", example = "Lima Green Community")
        @NotBlank String name,
        @Schema(description = "Community description", example = "Neighbors working together for a greener city.")
        String description,
        @Schema(description = "City or locality of the community", example = "Lima")
        @NotBlank String locality,
        @Schema(description = "Community icon URL", example = "https://example.com/community-icon.png")
        String icon_url) {}
