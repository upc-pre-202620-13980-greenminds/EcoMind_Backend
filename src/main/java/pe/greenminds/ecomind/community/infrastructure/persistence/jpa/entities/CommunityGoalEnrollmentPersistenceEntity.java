package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Getter
@Entity
@Table(name = "community_goal_enrollments")
public class CommunityGoalEnrollmentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected CommunityGoalEnrollmentPersistenceEntity() {
    }
}