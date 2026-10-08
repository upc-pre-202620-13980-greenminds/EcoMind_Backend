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

@Entity
@Check(
        name = "ck_achievementsharerequest",
        constraints =
                "requested_by > 0 AND ((status = 'PENDING' AND publication_id IS NULL AND"
                    + " confirmed_at IS NULL) OR (status = 'PUBLISHED' AND publication_id IS NOT"
                    + " NULL AND confirmed_at IS NOT NULL AND confirmed_at >= created_at))")
@Table(
        name = "achievement_share_requests",
        uniqueConstraints =
                @UniqueConstraint(name = "uq_share_publication", columnNames = "publication_id"))
@Getter
@Setter
@NoArgsConstructor
public class AchievementShareRequestPersistenceEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "award_id", nullable = false, length = 36)
    private String awardId;

    @Column(nullable = false)
    private Long requestedBy;

    @Column(nullable = false, length = 36)
    private String communityId;

    @Column(length = 36)
    private String publicationId;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant confirmedAt;

    @Version private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "award_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_share_award"))
    private AchievementAwardPersistenceEntity award;
}
