package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.quests.interfaces.acl.PublishedQuestsContextFacade.ValidatedAttempt;

import java.time.Instant;
import java.util.List;

/** Uses only the supplier public contract, never its internal model. */
public interface QuestServiceClient {
    List<ValidatedAttempt> findPublishedValidatedAttempts(
            Long userId, Long minigameId, Instant from, Instant to);

}
