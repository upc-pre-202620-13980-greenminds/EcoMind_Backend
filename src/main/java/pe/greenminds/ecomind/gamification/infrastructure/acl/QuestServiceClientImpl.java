package pe.greenminds.ecomind.gamification.infrastructure.acl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationDependencyUnavailableException;
import pe.greenminds.ecomind.gamification.application.outboundservices.QuestServiceClient;
import pe.greenminds.ecomind.quests.interfaces.acl.PublishedQuestsContextFacade;
import pe.greenminds.ecomind.quests.interfaces.acl.QuestsContextFacade;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the real supplier when implemented; absence fails explicitly, never as an empty/success
 * response.
 */
@Component
public class QuestServiceClientImpl implements QuestServiceClient {
    private final ObjectProvider<QuestsContextFacade> provider;
    private final PublishedQuestsContextFacade published;

    public QuestServiceClientImpl(
            ObjectProvider<QuestsContextFacade> provider, PublishedQuestsContextFacade published) {
        this.provider = provider;
        this.published = published;
    }

    public List<PublishedQuestsContextFacade.ValidatedAttempt> findPublishedValidatedAttempts(
            Long userId, Long minigameId, Instant from, Instant to) {
        return published.findValidatedAttempts(userId, minigameId, from, to);
    }

    public Optional<QuestsContextFacade.MinigameAttempt> findValidatedMinigameAttempt(
            UUID attemptId) {
        return requireSupplier().findValidatedMinigameAttempt(attemptId);
    }

    public List<QuestsContextFacade.MinigameAttempt> findValidatedAttempts(
            Long userId, UUID questId, Instant from, Instant to) {
        return requireSupplier().findValidatedAttempts(userId, questId, from, to);
    }

    private QuestsContextFacade requireSupplier() {
        var supplier = provider.getIfAvailable();
        if (supplier == null)
            throw new GamificationDependencyUnavailableException(
                    "Quests integration is not implemented yet");
        return supplier;
    }
}
