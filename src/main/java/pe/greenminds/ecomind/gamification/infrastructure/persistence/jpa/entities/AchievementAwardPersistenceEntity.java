package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ForeignKey;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "achievement_awards",
    uniqueConstraints = @UniqueConstraint(name = "uq_achievement_beneficiary",
        columnNames = {"achievement_id", "scope", "beneficiary_id"}),
    indexes = @Index(name = "ix_award_beneficiary", columnList = "scope,beneficiary_id,awarded_at"))
@Getter @Setter @NoArgsConstructor
public class AchievementAwardPersistenceEntity {
  @Id @Column(length = 36) private String id;
  @Column(name = "achievement_id", nullable = false, length = 36) private String achievementId;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "achievement_id", insertable = false, updatable = false,
      foreignKey = @ForeignKey(name = "fk_award_achievement"))
  private AchievementPersistenceEntity achievement;
  @Column(nullable = false, length = 16) private String scope;
  @Column(name = "beneficiary_id", nullable = false) private Long beneficiaryId;
  @Column(nullable = false, length = 36) private String sourceEventId;
  @Column(name = "awarded_at", nullable = false) private Instant awardedAt;
}
