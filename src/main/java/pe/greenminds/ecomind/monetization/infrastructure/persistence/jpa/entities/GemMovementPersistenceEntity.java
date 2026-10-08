package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "gem_movements",
    uniqueConstraints = @UniqueConstraint(name = "uq_gem_movement_reference", columnNames = "reference_id"),
    indexes = @Index(name = "ix_gem_movement_user_date", columnList = "user_id,occurred_at"))
@Getter
@Setter
@NoArgsConstructor
public class GemMovementPersistenceEntity {
  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, length = 12)
  private String type;

  @Column(nullable = false, length = 20)
  private String origin;

  @Column(nullable = false)
  private int amount;

  @Column(name = "balance_after", nullable = false)
  private int balanceAfter;

  @Column(name = "reference_id", nullable = false, length = 36)
  private String referenceId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;
}
