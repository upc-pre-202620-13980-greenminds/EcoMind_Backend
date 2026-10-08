package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "family_plan_items")
public class FamilyPlanItemPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name = "family_plan_id", nullable = false)
    private Long familyPlanId;

    @Column(name = "quest_id", nullable = false)
    private Long questId;

    @Column(name = "collaborative_session_id")
    private Long collaborativeSessionId;

    public Long getFamilyPlanId() { return familyPlanId; }
    public void setFamilyPlanId(Long familyPlanId) { this.familyPlanId = familyPlanId; }
    public Long getQuestId() { return questId; }
    public void setQuestId(Long questId) { this.questId = questId; }
    public Long getCollaborativeSessionId() { return collaborativeSessionId; }
    public void setCollaborativeSessionId(Long collaborativeSessionId) {
        this.collaborativeSessionId = collaborativeSessionId;
    }
}
