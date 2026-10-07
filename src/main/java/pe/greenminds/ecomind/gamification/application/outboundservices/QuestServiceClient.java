package pe.greenminds.ecomind.gamification.application.outboundservices;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import pe.greenminds.ecomind.quests.interfaces.acl.QuestsContextFacade;

/** Uses only the supplier public contract, never its internal model. */
public interface QuestServiceClient {
  Optional<QuestsContextFacade.MinigameAttempt> findValidatedMinigameAttempt(UUID attemptId);
  List<QuestsContextFacade.MinigameAttempt> findValidatedAttempts(Long userId, UUID questId, Instant from, Instant to);
}
