package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "community_achievements")
public class CommunityAchievementPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "community_goal_id")
    private Long communityGoalId;

    protected CommunityAchievementPersistenceEntity() {}

    public CommunityAchievementPersistenceEntity(Long communityId, String title, String description,
            Long communityGoalId) {
        this.communityId = communityId;
        this.title = title;
        this.description = description;
        this.communityGoalId = communityGoalId;
    }

    public Long getId() { return id; }
    public Long getCommunityId() { return communityId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getCommunityGoalId() { return communityGoalId; }
}
