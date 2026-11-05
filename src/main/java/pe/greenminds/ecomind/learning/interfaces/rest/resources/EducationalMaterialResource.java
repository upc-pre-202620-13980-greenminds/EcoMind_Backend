package pe.greenminds.ecomind.learning.interfaces.rest.resources;

import pe.greenminds.ecomind.learning.domain.model.MaterialCategory;
import pe.greenminds.ecomind.learning.domain.model.MaterialType;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.EducationalMaterialEntity;

public record EducationalMaterialResource(
    Long id,
    String title,
    String description,
    String content,
    MaterialType materialType,
    MaterialCategory category,
    String imageUrl,
    String videoUrl,
    Integer durationMinutes,
    String language,
    boolean downloadable,
    boolean favorite) {

  public static EducationalMaterialResource from(EducationalMaterialEntity entity, boolean favorite) {
    return new EducationalMaterialResource(entity.getId(), entity.getTitle(), entity.getDescription(),
        entity.getContent(), entity.getMaterialType(), entity.getCategory(), entity.getImageUrl(),
        entity.getVideoUrl(), entity.getDurationMinutes(), entity.getLanguage(),
        entity.isDownloadable(), favorite);
  }
}
