package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class CollabQuestSession {
    @Getter
    @Setter
    private Long id;

    private Long questId;
    private Long ownerId;
    private CollabQuestStatus status;
    private Date createdAt;
    private LocalDate startDate;
    private LocalDate endDate;

    public CollabQuestSession(
            Long id,
            Long questId,
            Long ownerId,
            CollabQuestStatus status,
            Date createdAt,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.id = id;
        this.questId = Objects.requireNonNull(questId, "questId must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.status = status;
        this.createdAt = createdAt;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CollabQuestSession(Long id, Long questId, Long ownerId, CollabQuestStatus status, LocalDate startDate, LocalDate endDate) {
        this(id, questId, ownerId, status, null, startDate, endDate);
    }

    public CollabQuestSession(Long id, Long questId, Long ownerId) {
        this.id = id;
        this.questId = Objects.requireNonNull(questId, "questId must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.status = CollabQuestStatus.PENDING;
        this.startDate = null;
        this.endDate = null;
    }

    public void start() {
        if (this.status != CollabQuestStatus.PENDING) {
            throw new IllegalStateException("Collaborative quest session must be PENDING");
        }

        this.status = CollabQuestStatus.STARTED;
        this.startDate = LocalDate.now();
    }

    public void complete() {
        complete(true);
    }

    public void completeAsFamilyPlanItem() {
        complete(false);
    }

    private void complete(boolean publishCompletion) {
        if (this.status != CollabQuestStatus.STARTED) {
            throw new IllegalStateException("Collaborative quest session must be STARTED");
        }

        this.status = CollabQuestStatus.COMPLETED;
        this.endDate = LocalDate.now();
    }

    public void cancel() {
        if (this.status != CollabQuestStatus.STARTED) {
            throw new IllegalStateException("Collaborative quest session must be STARTED");
        }

        this.status = CollabQuestStatus.CANCELLED;
        this.endDate = LocalDate.now();
    }

    public CollabQuestSession(Long questId, Long ownerId) {
        this(null, questId, ownerId);
    }

    public CollabQuestSession(Long questId, Long ownerId, CollabQuestStatus status, LocalDate startDate, LocalDate endDate) {
        this(null, questId, ownerId, status, startDate, endDate);
    }

    public Long getQuestId() {
        return questId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public CollabQuestStatus getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
