package pe.greenminds.ecomind.gamification.infrastructure.acl;

import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.QuestServiceClient;
import pe.greenminds.ecomind.quests.interfaces.acl.PublishedQuestsContextFacade;

import java.time.Instant;
import java.util.List;

@Component
public class QuestServiceClientImpl implements QuestServiceClient {
    private final PublishedQuestsContextFacade published;

    public QuestServiceClientImpl(PublishedQuestsContextFacade published) {
        this.published = published;
    }

    public List<PublishedQuestsContextFacade.ValidatedAttempt> findPublishedValidatedAttempts(
            Long userId, Long minigameId, Instant from, Instant to) {
        return published.findValidatedAttempts(userId, minigameId, from, to);
    }
}
