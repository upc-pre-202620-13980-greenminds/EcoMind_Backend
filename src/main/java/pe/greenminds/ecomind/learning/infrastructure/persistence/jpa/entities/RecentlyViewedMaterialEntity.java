package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recently_viewed_materials", uniqueConstraints =
    @UniqueConstraint(name = "uk_recent_material_user", columnNames = {"user_id", "material_id"}))
@Getter @Setter @NoArgsConstructor
public class RecentlyViewedMaterialEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "user_id", nullable = false) private Long userId;
  @Column(name = "material_id", nullable = false) private Long materialId;
  @Column(name = "viewed_at", nullable = false) private Instant viewedAt;
}
