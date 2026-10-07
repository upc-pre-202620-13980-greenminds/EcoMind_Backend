package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.FamilyPlanCompletedIntegrationEvent;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class QuestCompletionConsumer {
  private final RewardCommandService rewards;
  private final FamilyRewardCommandService familyRewards;
  public QuestCompletionConsumer(RewardCommandService rewards, FamilyRewardCommandService familyRewards) {
    this.rewards = rewards;
    this.familyRewards = familyRewards;
  }

  @EventListener
  public void on(QuestCompletedIntegrationEvent event) {
    var base = event.baseReward();
    rewards.handle(new GrantQuestRewardCommand(event.executionId(), new UserId(event.userId()),
        event.occurredAt(), event.activityDate(), event.countsForDailyStreak(),
        new Reward(base.ecopoints(), base.experience(), base.gems())));
  }

  @EventListener
  public void on(FamilyPlanCompletedIntegrationEvent event) {
    var result = familyRewards.handle(new GrantFamilyPlanRewardCommand(event.executionId(),
        new FamilyId(event.familyId()), event.additionalEcopoints(), event.occurredAt()));
    if (result.isFailure()) {
      throw new IllegalStateException("Cannot grant family reward for execution " + event.executionId());
    }
  }
}
