package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "favorite_materials", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "material_id"}))
@Getter @Setter @NoArgsConstructor
public class FavoriteMaterialEntity extends AuditableAbstractPersistenceEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "user_id", nullable = false) private Long userId;
  @Column(name = "material_id", nullable = false) private Long materialId;
}
