package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCollaborativeQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantCommunityRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantMinigameRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;

import java.util.List;

/** Called only by a trusted Quests integration handler after it validates completion. */
public interface RewardCommandService {
    RewardTransaction handle(GrantQuestRewardCommand command);

    RewardTransaction handle(GrantMinigameRewardCommand command);

    List<RewardTransaction> handle(GrantCollaborativeQuestRewardCommand command);

    List<RewardTransaction> handle(GrantCommunityRewardCommand command);
}
