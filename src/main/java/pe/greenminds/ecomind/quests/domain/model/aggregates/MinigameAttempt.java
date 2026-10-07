package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MinigameAttempt extends AbstractDomainAggregateRoot<MinigameAttempt> {
    @Getter
    @Setter
    private Long id;

    private Long userId;
    private Long questId;
    private Long minigameId;
    private Integer score;
    private MinigameAttemptStatus status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Map<String, Object> metadata;

    public MinigameAttempt(
            Long id,
            Long userId,
            Long questId,
            Long minigameId,
            Integer score,
            MinigameAttemptStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Map<String, Object> metadata
    ) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.questId = Objects.requireNonNull(questId, "questId must not be null");
        this.minigameId = Objects.requireNonNull(minigameId, "minigameId must not be null");
        this.score = score;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = endDate;
        this.metadata = copyMap(metadata);
    }

    public MinigameAttempt(Long userId, Long questId, Long minigameId) {
        this(
                null,
                userId,
                questId,
                minigameId,
                null,
                MinigameAttemptStatus.STARTED,
                OffsetDateTime.now(),
                null,
                Map.of()
        );
    }

    public void finish(Integer score, Map<String, Object> metadata) {
        if (status != MinigameAttemptStatus.STARTED) {
            throw new IllegalStateException("Minigame attempt must be STARTED");
        }
        this.score = Objects.requireNonNull(score, "score must not be null");
        this.metadata = copyMap(metadata);
        this.status = MinigameAttemptStatus.COMPLETED;
        this.endDate = OffsetDateTime.now();
    }

    public void cancel() {
        if (status != MinigameAttemptStatus.STARTED) {
            throw new IllegalStateException("Minigame attempt must be STARTED");
        }
        this.status = MinigameAttemptStatus.CANCELLED;
        this.endDate = OffsetDateTime.now();
    }

    public Long getUserId() { return userId; }
    public Long getQuestId() { return questId; }
    public Long getMinigameId() { return minigameId; }
    public Integer getScore() { return score; }
    public MinigameAttemptStatus getStatus() { return status; }
    public OffsetDateTime getStartDate() { return startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public Map<String, Object> getMetadata() { return metadata; }

    private static Map<String, Object> copyMap(Map<String, Object> map) {
        if (map == null) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
}
