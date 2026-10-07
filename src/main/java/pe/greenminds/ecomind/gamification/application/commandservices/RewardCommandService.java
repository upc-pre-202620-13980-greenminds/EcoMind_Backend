package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.entities.RewardTransaction;

/** Called only by a trusted Quests integration handler after it validates completion. */
public interface RewardCommandService {
  RewardTransaction handle(GrantQuestRewardCommand command);
}
