package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.quests.interfaces.acl.PublishedQuestsContextFacade.ValidatedAttempt;
import pe.greenminds.ecomind.quests.interfaces.acl.QuestsContextFacade;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Uses only the supplier public contract, never its internal model. */
public interface QuestServiceClient {
    List<ValidatedAttempt> findPublishedValidatedAttempts(
            Long userId, Long minigameId, Instant from, Instant to);

    Optional<QuestsContextFacade.MinigameAttempt> findValidatedMinigameAttempt(UUID attemptId);

    List<QuestsContextFacade.MinigameAttempt> findValidatedAttempts(
            Long userId, UUID questId, Instant from, Instant to);
}
