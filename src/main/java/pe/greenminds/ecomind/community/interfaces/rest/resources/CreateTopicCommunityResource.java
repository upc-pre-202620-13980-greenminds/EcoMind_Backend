package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTopicCommunityResource(
        @NotBlank String name,
        String description,
        @NotBlank String topic,
        @NotNull @Positive Integer member_limit,
        String icon_url) {}
