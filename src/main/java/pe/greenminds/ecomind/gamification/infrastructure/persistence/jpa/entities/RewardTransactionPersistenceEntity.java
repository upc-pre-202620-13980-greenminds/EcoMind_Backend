package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Check(
        name = "ck_rewardtransaction",
        constraints =
                "base_ecopoints >= 0 AND base_experience >= 0 AND base_gems >= 0 AND ecopoints >= 0"
                    + " AND experience >= 0 AND gems >= 0 AND applied_factor >= 0 AND"
                    + " repetition_factor BETWEEN 0 AND 1 AND source_type IN"
                    + " ('QUEST','MINIGAME','COLLABORATIVE_QUEST','FAMILY_PLAN','COMMUNITY_GOAL','COMMUNITY_EVENT')"
                    + " AND ((beneficiary_type = 'USER' AND user_progress_id IS NOT NULL AND"
                    + " user_progress_id = beneficiary_id AND family_score_id IS NULL) OR"
                    + " (beneficiary_type = 'FAMILY' AND family_score_id IS NOT NULL AND"
                    + " family_score_id = beneficiary_id AND user_progress_id IS NULL AND"
                    + " base_experience = 0 AND base_gems = 0 AND experience = 0 AND gems = 0))")
@Table(
        name = "reward_transactions",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_reward_origin",
                        columnNames = {
                            "source_type",
                            "source_execution_id",
                            "beneficiary_type",
                            "beneficiary_id"
                        }),
        indexes =
                @Index(
                        name = "ix_reward_beneficiary_date",
                        columnList = "beneficiary_type,beneficiary_id,occurred_at"))
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

    @Column(nullable = false)
    private long baseEcopoints;

    /** Deprecated schema column; base_ecopoints is authoritative. */
    @Column(name = "base_experience", nullable = false)
    private long legacyBaseExperience;

    @Column(nullable = false)
    private int baseGems;

    @Column(nullable = false)
    private long ecopoints;

    /** Deprecated schema column; ecopoints is authoritative. */
    @Column(name = "experience", nullable = false)
    private long legacyExperience;

    @Column(nullable = false)
    private int gems;

    @Column(length = 36)
    private String multiplierId;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal appliedFactor = BigDecimal.ONE;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal repetitionFactor = BigDecimal.ONE;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "user_progress_id")
    private Long userProgressId;

    @Column(name = "family_score_id")
    private Long familyScoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_progress_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_rewardtransaction_progress"))
    private UserProgressPersistenceEntity progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "family_score_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_rewardtransaction_family"))
    private FamilyScorePersistenceEntity familyScore;
}
