package pe.greenminds.ecomind.gamification.infrastructure.acl;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import pe.greenminds.ecomind.quests.interfaces.acl.QuestsContextFacade;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.gamification.application.outboundservices.QuestServiceClient;

/** Resolves the real supplier when implemented; absence fails explicitly, never as an empty/success response. */
@Component
public class QuestServiceClientImpl implements QuestServiceClient {
  private final ObjectProvider<QuestsContextFacade> provider;
  public QuestServiceClientImpl(ObjectProvider<QuestsContextFacade> provider) { this.provider = provider; }
  public Optional<QuestsContextFacade.MinigameAttempt> findValidatedMinigameAttempt(UUID attemptId) { return requireSupplier().findValidatedMinigameAttempt(attemptId); }
  public List<QuestsContextFacade.MinigameAttempt> findValidatedAttempts(Long userId, UUID questId, Instant from, Instant to) { return requireSupplier().findValidatedAttempts(userId, questId, from, to); }
  private QuestsContextFacade requireSupplier() {
    var supplier = provider.getIfAvailable();
    if (supplier == null) throw new IllegalStateException("Quests integration is not implemented yet");
    return supplier;
  }
}
