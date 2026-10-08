package pe.greenminds.ecomind.quests.interfaces.acl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pe.greenminds.ecomind.quests.interfaces.acl.resources.QuestRewardResource;

/** To be implemented by Quests. Missing data is not a zero-valued reward. */
public interface QuestsContextFacade {
  record MinigameAttempt(UUID attemptId, UUID questId, Long userId, long score,
      Instant occurredAt, QuestRewardResource baseReward) {}
  Optional<MinigameAttempt> findValidatedMinigameAttempt(UUID attemptId);
  /** Inclusive start and exclusive end; returns validated attempts only. */
  List<MinigameAttempt> findValidatedAttempts(Long userId, UUID questId, Instant from, Instant to);
}
