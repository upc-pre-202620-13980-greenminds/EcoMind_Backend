package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "achievements")
@Getter @Setter @NoArgsConstructor
public class AchievementPersistenceEntity {
  @Id @Column(length = 36) private String id;
  @Column(nullable = false, unique = true, length = 80) private String code;
  @Column(nullable = false, length = 120) private String name;
  @Column(nullable = false, length = 500) private String description;
  @Column(nullable = false, length = 16) private String scope;
  @Column(nullable = false, length = 40) private String metric;
  @Column(nullable = false) private long target;
  @Column(nullable = false) private boolean active;
}
