package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "reward_transactions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_reward_origin",
        columnNames = {"source_type", "source_execution_id", "beneficiary_type", "beneficiary_id"}),
    indexes = @Index(name = "ix_reward_beneficiary_date", columnList = "beneficiary_type,beneficiary_id,occurred_at"))
public class RewardTransactionPersistenceEntity {
  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "source_type", nullable = false, length = 24)
  private String sourceType;

  @Column(name = "source_execution_id", nullable = false, length = 36)
  private String sourceExecutionId;

  @Column(name = "beneficiary_type", nullable = false, length = 8)
  private String beneficiaryType;

  @Column(name = "beneficiary_id", nullable = false)
  private Long beneficiaryId;

  @Column(nullable = false) private long baseEcopoints;
  @Column(nullable = false) private long baseExperience;
  @Column(nullable = false) private int baseGems;
  @Column(nullable = false) private long ecopoints;
  @Column(nullable = false) private long experience;
  @Column(nullable = false) private int gems;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;
}
