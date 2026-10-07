package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Stores a user profile. The id is not generated: it is the account id issued by IAM, kept as a
 * plain value without a foreign key to the IAM tables.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {

  @Id
  private Long userId;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, length = 20)
  private String socialRole;

  @Column(nullable = false)
  private int streak;

  private LocalDate lastStreakDate;

  @Column(nullable = false)
  private int ecopoints;

  @Column(nullable = false)
  private int gemBalance;

  private Long equippedCosmeticId;
}
