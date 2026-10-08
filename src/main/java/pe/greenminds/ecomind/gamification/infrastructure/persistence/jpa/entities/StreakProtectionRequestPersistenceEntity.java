package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Check;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Check(
        name = "ck_streakprotectionrequest",
        constraints =
                "(status = 'PENDING' AND resolved_at IS NULL) OR (status IN"
                    + " ('PROTECTED','UNAVAILABLE') AND resolved_at IS NOT NULL AND resolved_at >="
                    + " created_at)")
@Table(
        name = "streak_protection_requests",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_streak_user_day",
                        columnNames = {"user_id", "streak_date"}))
@Getter
@Setter
@NoArgsConstructor
public class StreakProtectionRequestPersistenceEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate streakDate;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant resolvedAt;

    @Version private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_protection_progress"))
    private UserProgressPersistenceEntity progress;
}
