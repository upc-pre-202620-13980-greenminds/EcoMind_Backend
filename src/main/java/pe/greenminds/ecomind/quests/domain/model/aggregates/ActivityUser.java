package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.events.ActivityUserCreatedEvent;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityUser extends AbstractDomainAggregateRoot<ActivityUser> {

    @Getter
    @Setter
    private Long id;

    private Long questUserId;
    private Long activityId;
    private Double progress;
    private LocalDate endDate;
    private String activityDescription;
    private Map<String, Object> activityConfiguration;
    private Long collaborativeSessionId;

    public ActivityUser(
            Long id,
            Long userId,
            Long activityId,
            Double progress,
            LocalDate endDate,
            String activityDescription,
            Map<String, Object> activityConfiguration,
            Long collaborativeSessionId
    ) {
        this.id = id;
        this.questUserId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.activityId = Objects.requireNonNull(activityId, "Activity ID cannot be null");
        this.progress = progress;
        this.endDate = endDate;
        this.activityDescription = activityDescription;
        this.activityConfiguration = copyConfiguration(activityConfiguration);
        this.collaborativeSessionId = collaborativeSessionId;
    }

    public ActivityUser(
            Long id,
            Long userId,
            Long activityId,
            String activityDescription,
            Map<String, Object> activityConfiguration,
            Long collaborativeSessionId
    ) {
        this.id = id;
        this.questUserId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.activityId = Objects.requireNonNull(activityId, "Activity ID cannot be null");
        this.progress = 0.0;
        this.endDate = null;
        this.activityDescription = activityDescription;
        this.activityConfiguration = copyConfiguration(activityConfiguration);
        this.collaborativeSessionId = collaborativeSessionId;
    }

    public ActivityUser(
            Long userId,
            Long activityId,
            Double progress,
            LocalDate endDate,
            String activityDescription,
            Map<String, Object> activityConfiguration,
            Long collaborativeSessionId
    ) {
        this(
                null,
                userId,
                activityId,
                progress,
                endDate,
                activityDescription,
                activityConfiguration,
                collaborativeSessionId
        );
    }

    public ActivityUser(
            Long userId,
            Long activityId,
            String activityDescription,
            Map<String, Object> activityConfiguration,
            Long collaborativeSessionId
    ) {
        this(
                null,
                userId,
                activityId,
                activityDescription,
                activityConfiguration,
                collaborativeSessionId
        );
    }

    public void onCreated(){
        registerDomainEvent(ActivityUserCreatedEvent.from(this));
    }

    public void updateProgress(Double progress) {
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException(
                    "Progress must be between 0 and 100"
            );
        }

        this.progress = progress;

        if (progress >= 100.0) {
            complete();
        } else {
            this.endDate = null;
        }
    }

    private void complete() {
        this.progress = 100.0;
        this.endDate = LocalDate.now();
    }

    public Long getQuestUserId() {
        return questUserId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public Double getProgress() {
        return progress;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public Map<String, Object> getActivityConfiguration() {
        return activityConfiguration;
    }

    public Long getCollaborativeSessionId() {
        return collaborativeSessionId;
    }

    private static Map<String, Object> copyConfiguration(
            Map<String, Object> activityConfiguration
    ) {
        if (activityConfiguration == null) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(activityConfiguration));
    }
}
