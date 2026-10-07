package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.util.Map;

@Entity
@Table(name="activities")
public class ActivityPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "questId", nullable=false)
    private Long questId;

    @Column(name = "description")
    private String description;

    @Positive
    @Column(name = "position", nullable=false)
    private Integer order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable=false)
    private ActivityType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activity_configuration", columnDefinition = "jsonb")
    private Map<String, Object> activityConfiguration;

    @Column(name = "image")
    private String image;

    public Long getQuestId() {
        return questId;
    }

    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public Map<String, Object> getActivityConfiguration() {
        return activityConfiguration;
    }

    public void setActivityConfiguration(Map<String, Object> activityConfiguration) {
        this.activityConfiguration = activityConfiguration;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
