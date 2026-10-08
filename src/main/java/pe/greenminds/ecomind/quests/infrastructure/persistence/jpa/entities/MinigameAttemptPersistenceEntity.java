package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "minigame_attempts")
public class MinigameAttemptPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quest_id", nullable = false)
    private Long questId;

    @Column(name = "minigame_id", nullable = false)
    private Long minigameId;

    @Column(name = "score")
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MinigameAttemptStatus status;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "successful")
    private Boolean successful;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getQuestId() { return questId; }
    public void setQuestId(Long questId) { this.questId = questId; }
    public Long getMinigameId() { return minigameId; }
    public void setMinigameId(Long minigameId) { this.minigameId = minigameId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public MinigameAttemptStatus getStatus() { return status; }
    public void setStatus(MinigameAttemptStatus status) { this.status = status; }
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Boolean getSuccessful() { return successful; }
    public void setSuccessful(Boolean successful) { this.successful = successful; }
}
