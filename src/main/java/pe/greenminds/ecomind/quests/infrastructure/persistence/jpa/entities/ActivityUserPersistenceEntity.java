package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "activity_user")
public class ActivityUserPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name="questUser_id", nullable = false)
    private Long questUserId;

    @Column(name="activity_id", nullable = false)
    private Long activityId;

    @Column(name="progress", nullable = false)
    private Double progress;

    @Column(name="endDate")
    private LocalDate endDate;

    @Column(name = "activity_description")
    private String activityDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activity_configuration", columnDefinition = "jsonb")
    private Map<String, Object> activityConfiguration;

    @Column(name="collaborativeSession_id")
    private Long collaborativeSessionId;

    public Long getQuestUserId() {
        return questUserId;
    }

    public void setQuestUserId(Long userId) {
        this.questUserId = userId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public Map<String, Object> getActivityConfiguration() {
        return activityConfiguration;
    }

    public void setActivityConfiguration(Map<String, Object> activityConfiguration) {
        this.activityConfiguration = activityConfiguration;
    }

    public Long getCollaborativeSessionId() {
        return collaborativeSessionId;
    }

    public void setCollaborativeSessionId(Long collaborativeSessionId) {
        this.collaborativeSessionId = collaborativeSessionId;
    }
}
