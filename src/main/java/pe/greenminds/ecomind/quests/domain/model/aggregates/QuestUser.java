package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.events.QuestUserCreatedEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class QuestUser extends AbstractDomainAggregateRoot<QuestUser> {
    private static final ZoneId DAILY_ZONE = ZoneId.of("America/Lima");

    @Getter
    @Setter
    private Long id;

    private Long userId;
    private Long questId;
    private QuestStatus status;
    private Double progress;
    private LocalDate endDate;
    private Long CollaborativeSessionId;

    public QuestUser(Long id, Long userId, Long questId, Long CollaborativeSessionId) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.questId = Objects.requireNonNull(questId, "questId is required");
        this.status = QuestStatus.IN_PROGRESS;
        this.progress = 0.0;
        this.endDate = null;
        this.CollaborativeSessionId = CollaborativeSessionId;
    }

    public QuestUser(Long id, Long userId, Long questId, QuestStatus status, Double progress, LocalDate endDate, Long CollaborativeSessionId) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.questId = Objects.requireNonNull(questId, "questId is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.progress = Objects.requireNonNull(progress, "progress is required");
        this.endDate = endDate;
        this.CollaborativeSessionId = CollaborativeSessionId;
    }

    public QuestUser(Long userId, Long questId, QuestStatus status, Double progress, LocalDate endDate, Long CollaborativeSessionId){
        this(null, userId, questId, status, progress, endDate, CollaborativeSessionId);
    }

    public QuestUser(Long userId, Long questId, Long CollaborativeSessionId){
        this(null, userId, questId, CollaborativeSessionId);
    }

    public void onCreated(){
    }

    public void updateProgress(Double progress) {
        if (status == QuestStatus.COMPLETED || status == QuestStatus.CANCELLED) {
            throw new IllegalStateException("A completed or cancelled quest assignment cannot change progress");
        }
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException(
                    "Progress must be between 0 and 100"
            );
        }

        this.progress = progress;

        if (progress >= 100.0 && this.status != QuestStatus.EXPIRED) {
            readyToComplete();
        } else if (this.status != QuestStatus.COMPLETED && this.status != QuestStatus.EXPIRED) {
            this.status = QuestStatus.IN_PROGRESS;
            this.endDate = null;
        }
    }

    private void readyToComplete() {
        this.progress = 100.0;
        this.status = QuestStatus.READY_TO_COMPLETE;
        this.endDate = null;
    }

    public void complete() {
        if (this.status != QuestStatus.READY_TO_COMPLETE
                && !(this.status == QuestStatus.EXPIRED && this.progress >= 100.0)) {
            throw new IllegalStateException("Quest status must be READY_TO_COMPLETE or EXPIRED with 100 progress");
        }

        this.progress = 100.0;
        this.status = QuestStatus.COMPLETED;
        this.endDate = LocalDate.now(DAILY_ZONE);
    }

    public void expire() {
        if (this.status != QuestStatus.COMPLETED && this.status != QuestStatus.CANCELLED) {
            this.status = QuestStatus.EXPIRED;
        }
    }

    public void cancel() {
        if (status == QuestStatus.COMPLETED) {
            throw new IllegalStateException("A completed quest assignment cannot be cancelled");
        }
        if (status == QuestStatus.CANCELLED) {
            throw new IllegalStateException("Quest assignment is already cancelled");
        }
        status = QuestStatus.CANCELLED;
        endDate = LocalDate.now(DAILY_ZONE);
    }

    public boolean isExpired() {
        return this.status == QuestStatus.EXPIRED;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getQuestId() {
        return questId;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public Double getProgress() {
        return progress;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Long getCollaborativeSessionId() {
        return CollaborativeSessionId;
    }
}
