package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTopicCommunityResource(
        @Schema(description = "Name of the topic community", example = "Water Guardians")
        @NotBlank String name,
        @Schema(description = "Community description", example = "A community dedicated to saving water.")
        String description,
        @Schema(description = "Community topic", example = "water")
        @NotBlank String topic,
        @Schema(description = "Maximum number of members", example = "100")
        @NotNull @Positive Integer member_limit,
        @Schema(description = "Community icon URL", example = "https://example.com/water-community.png")
        String icon_url) {}
