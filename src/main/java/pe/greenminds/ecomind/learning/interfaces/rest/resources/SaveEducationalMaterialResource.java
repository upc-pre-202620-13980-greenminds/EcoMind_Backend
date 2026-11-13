package pe.greenminds.ecomind.learning.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import pe.greenminds.ecomind.learning.domain.model.MaterialCategory;
import pe.greenminds.ecomind.learning.domain.model.MaterialType;
import pe.greenminds.ecomind.learning.domain.model.RecommendedAge;

public record SaveEducationalMaterialResource(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 500) String description,
    @NotBlank String content,
    @NotNull MaterialType materialType,
    @NotNull MaterialCategory category,
    @Size(max = 1000) String imageUrl,
    @Size(max = 1000) String videoUrl,
    @NotNull @Min(1) @Max(180) Integer durationMinutes,
    @NotBlank @Pattern(regexp = "[a-z]{2}(-[A-Z]{2})?") String language,
    boolean downloadable,
    @Size(max = 160) String sourceName,
    @Size(max = 1000) String sourceUrl,
    @Size(max = 160) String author,
    @Size(max = 160) String license,
    RecommendedAge recommendedAge) {}
