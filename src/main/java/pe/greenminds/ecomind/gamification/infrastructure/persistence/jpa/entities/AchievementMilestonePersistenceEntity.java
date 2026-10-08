package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "achievement_milestones",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_achievement_milestone",
                        columnNames = {"metric", "beneficiary", "execution_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AchievementMilestonePersistenceEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 40)
    private String metric;

    @Column(nullable = false, length = 48)
    private String beneficiary;

    @Column(nullable = false, length = 36)
    private String executionId;
}
