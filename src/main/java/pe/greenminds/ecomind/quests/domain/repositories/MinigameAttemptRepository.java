package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MinigameAttemptRepository {
    MinigameAttempt save(MinigameAttempt minigameAttempt);
    Optional<MinigameAttempt> findById(Long id);
    boolean existsByUserIdAndStatus(Long userId, MinigameAttemptStatus status);
    List<MinigameAttempt> findByUserIdAndMinigameId(Long userId, Long minigameId);
    List<MinigameAttempt> findRewardedAttemptsSince(
            Long userId,
            Long minigameId,
            OffsetDateTime since
    );
    int countRewardedCompletedByUserIds(List<Long> userIds);
    void deleteByMinigameId(Long minigameId);
}
