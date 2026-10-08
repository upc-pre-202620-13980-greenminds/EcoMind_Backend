package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@org.hibernate.annotations.Check(
        name = "ck_outbox_attempts",
        constraints = "attempts >= 0 AND gems >= 0")
@Table(
        name = "\"gamification_outbox\"",
        indexes =
                @Index(
                        name = "ix_gamification_outbox_pending",
                        columnList = "delivered_at,next_attempt_at"),
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_gamification_message",
                        columnNames = {"message_type", "correlation_id"}))
@Getter
@Setter
@NoArgsConstructor
public class GamificationOutboxPersistenceEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 40)
    private String messageType;

    @Column(nullable = false, length = 36)
    private String correlationId;

    private Long userId;

    @Column(length = 36)
    private String achievementId;

    @Column(length = 36)
    private String awardId;

    @Column(length = 36)
    private String communityId;

    @Column(length = 36)
    private String cosmeticId;

    private int gems;
    private LocalDate streakDate;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    private Instant deliveredAt;
    private int attempts;
    @Version private long version;
}
