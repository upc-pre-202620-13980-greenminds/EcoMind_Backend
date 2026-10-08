package pe.greenminds.ecomind.gamification.interfaces.acl;

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

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class QuestCompletionConsumer {
    private final RewardCommandService rewards;
    private final FamilyRewardCommandService familyRewards;
    private final QuestServiceClient quests;

    public QuestCompletionConsumer(
            RewardCommandService rewards,
            FamilyRewardCommandService familyRewards,
            QuestServiceClient quests) {
        this.rewards = rewards;
        this.quests = quests;
        this.familyRewards = familyRewards;
    }

    @EventListener
    public void on(QuestCompletedIntegrationEvent event) {
        var base = event.baseReward();
        rewards.handle(
                new GrantQuestRewardCommand(
                        event.executionId(),
                        new UserId(event.userId()),
                        event.occurredAt(),
                        event.activityDate(),
                        event.countsForDailyStreak(),
                        new Reward(base.ecopoints(), base.experience(), base.gems())));
    }

    @EventListener
    public void on(MinigameCompletedIntegrationEvent e) {
        var attempt =
                quests.findValidatedMinigameAttempt(e.attemptId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Validated attempt not found"));
        if (!attempt.questId().equals(e.questId())
                || !attempt.userId().equals(e.userId())
                || attempt.score() != e.score()
                || !attempt.occurredAt().equals(e.occurredAt()))
            throw new IllegalArgumentException("Attempt facts do not match completion");
        var r = attempt.baseReward();
        rewards.handle(
                new GrantMinigameRewardCommand(
                        e.attemptId(),
                        e.questId(),
                        new UserId(e.userId()),
                        e.occurredAt(),
                        new Reward(r.ecopoints(), r.experience(), r.gems())));
    }

    @EventListener
    public void on(CollaborativeQuestCompletedIntegrationEvent e) {
        var r = e.rewardPerParticipant();
        rewards.handle(
                new GrantCollaborativeQuestRewardCommand(
                        e.sessionId(),
                        e.participantIds().stream().map(UserId::new).toList(),
                        e.occurredAt(),
                        new Reward(r.ecopoints(), r.experience(), r.gems())));
    }

    @EventListener
    public void on(FamilyPlanCompletedIntegrationEvent event) {
        var result =
                familyRewards.handle(
                        new GrantFamilyPlanRewardCommand(
                                event.executionId(),
                                new FamilyId(event.familyId()),
                                event.additionalEcopoints(),
                                event.occurredAt()));
        if (result.isFailure()) {
            throw new IllegalStateException(
                    "Cannot grant family reward for execution " + event.executionId());
        }
    }
}
