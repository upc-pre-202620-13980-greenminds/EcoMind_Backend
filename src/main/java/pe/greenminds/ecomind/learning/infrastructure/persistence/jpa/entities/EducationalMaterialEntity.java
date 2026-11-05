package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.learning.domain.model.MaterialCategory;
import pe.greenminds.ecomind.learning.domain.model.MaterialType;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "educational_materials")
@Getter @Setter @NoArgsConstructor
public class EducationalMaterialEntity extends AuditableAbstractPersistenceEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 160) private String title;
  @Column(nullable = false, length = 500) private String description;
  @Column(nullable = false, columnDefinition = "TEXT") private String content;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private MaterialType materialType;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private MaterialCategory category;
  @Column(length = 1000) private String imageUrl;
  @Column(length = 1000) private String videoUrl;
  @Column(nullable = false) private Integer durationMinutes;
  @Column(nullable = false, length = 8) private String language;
  @Column(nullable = false) private boolean downloadable;
  @Column(nullable = false) private boolean active = true;
}
