package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "gem_packages")
@Getter @Setter @NoArgsConstructor
public class GemPackagePersistenceEntity extends AuditableAbstractPersistenceEntity {
  @Id @Column(length = 36) private String id;
  @Column(nullable = false, length = 120) private String name;
  @Column(nullable = false) private int gemAmount;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
  @Column(nullable = false, length = 3) private String currency;
  @Column(nullable = false) private boolean active;
}
