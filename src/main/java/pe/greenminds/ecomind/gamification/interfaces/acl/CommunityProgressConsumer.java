package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventCompletedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalCompletedIntegrationEvent;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCommunityRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementMilestoneRepository;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class CommunityProgressConsumer {
    private final RewardCommandService rewards;
    private final AchievementMilestoneRepository milestones;
    private final AchievementCommandService achievements;

    public CommunityProgressConsumer(
            RewardCommandService rewards,
            AchievementCommandService achievements,
            AchievementMilestoneRepository milestones) {
        this.rewards = rewards;
        this.achievements = achievements;
        this.milestones = milestones;
    }

    @EventListener
    public void on(CommunityEventCompletedIntegrationEvent e) {
        if (e.configuredReward() != null && !e.eligibleParticipantIds().isEmpty()) {
            var r = e.configuredReward();
            rewards.handle(
                    new GrantCommunityRewardCommand(
                            RewardSourceType.COMMUNITY_EVENT,
                            e.executionId(),
                            e.eligibleParticipantIds().stream().map(UserId::new).toList(),
                            e.occurredAt(),
                            new Reward(r.ecopoints(), r.gems())));
        }
    }

    @EventListener
    public void on(CommunityGoalCompletedIntegrationEvent e) {
        milestones.lock("COMMUNITY:" + e.communityId());
        if (e.configuredReward() != null && !e.eligibleParticipantIds().isEmpty()) {
            var r = e.configuredReward();
            rewards.handle(
                    new GrantCommunityRewardCommand(
                            RewardSourceType.COMMUNITY_GOAL,
                            e.executionId(),
                            e.eligibleParticipantIds().stream().map(UserId::new).toList(),
                            e.occurredAt(),
                            new Reward(r.ecopoints(), r.gems())));
        }
        achievements.evaluateCommunity(
                e.communityId(), e.eligibleParticipantIds(), e.executionId(), e.occurredAt());
    }
}
