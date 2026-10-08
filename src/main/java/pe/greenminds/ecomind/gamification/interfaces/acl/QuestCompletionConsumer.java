package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationDependencyUnavailableException;
import pe.greenminds.ecomind.gamification.application.outboundservices.QuestServiceClient;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCollaborativeQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantMinigameRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.QuestExperienceRepository;
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
    private final QuestExperienceRepository experience;
    private final ZoneId zone;
    private final QuestServiceClient quests;

    public QuestCompletionConsumer(
            RewardCommandService rewards,
            FamilyRewardCommandService families,
            QuestExperienceRepository experience,
            QuestServiceClient quests,
            @Value("${gamification.activity-zone:America/Lima}") String zone) {
        this.rewards = rewards;
        this.families = families;
        this.experience = experience;
        this.zone = ZoneId.of(zone);
        this.quests = quests;
    }

    @EventListener
    public void on(QuestCompletedIntegrationEvent e) {
        var at = e.completedAt().toInstant();
        rewards.handle(
                new GrantQuestRewardCommand(
                        execution("quest-user", e.questUserId()),
                        new UserId(e.userId()),
                        at,
                        at.atZone(zone).toLocalDate(),
                        "DAILY_QUEST".equals(e.questType().name()),
                        base(e.questId(), e.baseEcopoints(), e.baseGems())));
    }

    @EventListener
    public void on(MinigameCompletedIntegrationEvent e) {
        var at = e.completedAt().toInstant();
        long prior =
                quests
                        .findPublishedValidatedAttempts(
                                e.userId(), e.minigameId(), at.minus(Duration.ofHours(3)), at)
                        .stream()
                        .filter(a -> !a.attemptId().equals(e.attemptId()))
                        .count();
        rewards.handle(
                new GrantMinigameRewardCommand(
                        execution("minigame-attempt", e.attemptId()),
                        execution("minigame", e.minigameId()),
                        new UserId(e.userId()),
                        at,
                        base(e.questId(), e.baseEcopoints(), e.baseGems()),
                        prior));
    }

    @EventListener
    public void on(CollaborativeQuestCompletedIntegrationEvent e) {
        rewards.handle(
                new GrantCollaborativeQuestRewardCommand(
                        execution("collaborative-session", e.sessionId()),
                        e.participantUserIds().stream().map(UserId::new).toList(),
                        e.completedAt().toInstant(),
                        base(e.questId(), e.baseEcopoints(), e.baseGems())));
    }

    @EventListener
    public void on(FamilyPlanCompletedIntegrationEvent e) {
        var execution = execution("family-plan", e.familyPlanId());
        var family = new FamilyId(e.familyId());
        // Quests does not configure an additional plan bonus in this published contract.
        var result =
                families.handle(
                        new GrantFamilyPlanRewardCommand(
                                execution, family, 0, e.completedAt().toInstant()));
        if (result.isFailure())
            throw new IllegalStateException("Unknown family for completed plan");
    }

    private Reward base(Long quest, Integer points, Integer gems) {
        long xp =
                experience
                        .find(quest)
                        .orElseThrow(
                                () ->
                                        new GamificationDependencyUnavailableException(
                                                "Configure base experience for quest version "
                                                        + quest
                                                        + " before completion"));
        return new Reward(Objects.requireNonNull(points), xp, Objects.requireNonNull(gems));
    }

    private UUID execution(String type, Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Execution identifier must be positive");
        return UUID.nameUUIDFromBytes((type + ":" + id).getBytes(StandardCharsets.UTF_8));
    }
}
