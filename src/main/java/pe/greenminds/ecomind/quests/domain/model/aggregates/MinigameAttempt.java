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
    private Integer givenGems;
    private Integer givenEcopoints;

    public MinigameAttempt(
            Long id,
            Long userId,
            Long questId,
            Long minigameId,
            Integer score,
            MinigameAttemptStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Map<String, Object> metadata,
            Integer givenGems,
            Integer givenEcopoints
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
        this.givenGems = givenGems == null ? 0 : givenGems;
        this.givenEcopoints = givenEcopoints == null ? 0 : givenEcopoints;
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
                Map.of(),
                0,
                0
        );
    }

    public void finish(Integer score, Map<String, Object> metadata, Integer givenGems, Integer givenEcopoints) {
        if (status != MinigameAttemptStatus.STARTED) {
            throw new IllegalStateException("Minigame attempt must be STARTED");
        }
        this.score = Objects.requireNonNull(score, "score must not be null");
        this.metadata = copyMap(metadata);
        this.givenGems = Objects.requireNonNull(givenGems, "givenGems must not be null");
        this.givenEcopoints = Objects.requireNonNull(givenEcopoints, "givenEcopoints must not be null");
        this.status = MinigameAttemptStatus.COMPLETED;
        this.endDate = OffsetDateTime.now();
    }

    public void cancel() {
        if (status != MinigameAttemptStatus.STARTED) {
            throw new IllegalStateException("Minigame attempt must be STARTED");
        }
        this.status = MinigameAttemptStatus.CANCELLED;
        this.endDate = OffsetDateTime.now();
        this.givenGems = 0;
        this.givenEcopoints = 0;
    }

    public Long getUserId() { return userId; }
    public Long getQuestId() { return questId; }
    public Long getMinigameId() { return minigameId; }
    public Integer getScore() { return score; }
    public MinigameAttemptStatus getStatus() { return status; }
    public OffsetDateTime getStartDate() { return startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Integer getGivenGems() { return givenGems; }
    public Integer getGivenEcopoints() { return givenEcopoints; }

    private static Map<String, Object> copyMap(Map<String, Object> map) {
        if (map == null) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
}
