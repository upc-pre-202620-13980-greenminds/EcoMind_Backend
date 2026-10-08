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

import java.time.Instant;

@Entity
@Table(
        name = "achievement_awards",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_achievement_beneficiary",
                    columnNames = {"achievement_id", "scope", "beneficiary_id"}),
            @UniqueConstraint(
                    name = "uq_achievement_community",
                    columnNames = {"achievement_id", "community_id"})
        },
        indexes =
                @Index(
                        name = "ix_award_beneficiary",
                        columnList = "scope,beneficiary_id,awarded_at"))
@Check(
        constraints =
                "(scope = 'COMMUNITY' AND community_id IS NOT NULL AND beneficiary_id IS NULL AND"
                    + " user_progress_id IS NULL AND family_score_id IS NULL) OR (scope ="
                    + " 'INDIVIDUAL' AND beneficiary_id IS NOT NULL AND beneficiary_id > 0 AND"
                    + " user_progress_id IS NOT NULL AND user_progress_id = beneficiary_id AND"
                    + " family_score_id IS NULL AND community_id IS NULL) OR (scope = 'FAMILY' AND"
                    + " beneficiary_id IS NOT NULL AND beneficiary_id > 0 AND family_score_id IS"
                    + " NOT NULL AND family_score_id = beneficiary_id AND user_progress_id IS NULL"
                    + " AND community_id IS NULL)")
@Getter
@Setter
@NoArgsConstructor
public class AchievementAwardPersistenceEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "achievement_id", nullable = false, length = 36)
    private String achievementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "achievement_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_award_achievement"))
    private AchievementPersistenceEntity achievement;

    @Column(nullable = false, length = 16)
    private String scope;

    @Column(name = "beneficiary_id")
    private Long beneficiaryId;

    @Column(length = 36)
    private String communityId;

    @Column(nullable = false, length = 36)
    private String sourceEventId;

    @Column(name = "awarded_at", nullable = false)
    private Instant awardedAt;

    @Column(name = "user_progress_id")
    private Long userProgressId;

    @Column(name = "family_score_id")
    private Long familyScoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_progress_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_achievementaward_progress"))
    private UserProgressPersistenceEntity progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "family_score_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_achievementaward_family"))
    private FamilyScorePersistenceEntity familyScore;
}
