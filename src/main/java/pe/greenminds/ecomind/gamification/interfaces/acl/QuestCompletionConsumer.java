package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.QuestServiceClient;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCollaborativeQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantMinigameRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.quests.interfaces.acl.events.CollaborativeQuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.MinigameCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class QuestCompletionConsumer {
    private final RewardCommandService rewards;
    private final FamilyRewardCommandService families;
    private final ZoneId zone;
    private final QuestServiceClient quests;

    public QuestCompletionConsumer(
            RewardCommandService rewards,
            FamilyRewardCommandService families,
            QuestServiceClient quests,
            @Value("${gamification.activity-zone:America/Lima}") String zone) {
        this.rewards = rewards;
        this.families = families;
        this.zone = ZoneId.of(zone);
        this.quests = quests;
    }

    @EventListener
    public void on(QuestCompletedIntegrationEvent event) {
        var completedAt = event.completedAt().toInstant();
        rewards.handle(new GrantQuestRewardCommand(
                execution("quest-user", event.questUserId()),
                new UserId(event.userId()),
                completedAt,
                completedAt.atZone(zone).toLocalDate(),
                "DAILY_QUEST".equals(event.questType().name()),
                base(event.baseEcopoints(), event.baseGems())));
    }

    @EventListener
    public void on(MinigameCompletedIntegrationEvent event) {
        var completedAt = event.completedAt().toInstant();
        long priorAttempts = quests.findPublishedValidatedAttempts(
                        event.userId(), event.minigameId(),
                        completedAt.minus(Duration.ofHours(3)), completedAt)
                .stream()
                .filter(attempt -> !attempt.attemptId().equals(event.attemptId()))
                .count();
        rewards.handle(new GrantMinigameRewardCommand(
                execution("minigame-attempt", event.attemptId()),
                execution("minigame", event.minigameId()),
                new UserId(event.userId()),
                completedAt,
                base(event.baseEcopoints(), event.baseGems()),
                priorAttempts));
    }

    @EventListener
    public void on(CollaborativeQuestCompletedIntegrationEvent event) {
        rewards.handle(new GrantCollaborativeQuestRewardCommand(
                execution("collaborative-session", event.sessionId()),
                event.participantUserIds().stream().map(UserId::new).toList(),
                event.completedAt().toInstant(),
                base(event.baseEcopoints(), event.baseGems())));
    }

    @EventListener
    public void on(FamilyPlanCompletedIntegrationEvent event) {
        var result = families.handle(new GrantFamilyPlanRewardCommand(
                execution("family-plan", event.familyPlanId()),
                new FamilyId(event.familyId()),
                0,
                event.completedAt().toInstant()));
        if (result.isFailure()) {
            throw new IllegalStateException("Unknown family for completed plan");
        }
    }

    private Reward base(Integer ecopoints, Integer gems) {
        return new Reward(Objects.requireNonNull(ecopoints), Objects.requireNonNull(gems));
    }

    private UUID execution(String type, Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Execution identifier must be positive");
        }
        return UUID.nameUUIDFromBytes((type + ":" + id).getBytes(StandardCharsets.UTF_8));
    }
}
