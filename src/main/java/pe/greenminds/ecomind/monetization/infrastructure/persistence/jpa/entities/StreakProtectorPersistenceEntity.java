package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "streak_protectors")
@Getter @Setter @NoArgsConstructor
public class StreakProtectorPersistenceEntity extends AuditableAbstractPersistenceEntity {
  @Id @Column(length = 36) private String id;
  @Column(nullable = false, length = 120) private String name;
  @Column(nullable = false, length = 500) private String description;
  @Column(nullable = false) private int priceInGems;
  @Column(nullable = false) private boolean active;
}
