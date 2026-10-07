package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.entities.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardTransactionRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;

@Service
public class RewardCommandServiceImpl implements RewardCommandService {

  private final UserProgressRepository progressRepository;
  private final RewardTransactionRepository rewardRepository;

  public RewardCommandServiceImpl(
      UserProgressRepository progressRepository, RewardTransactionRepository rewardRepository) {
    this.progressRepository = progressRepository;
    this.rewardRepository = rewardRepository;
  }

  @Override
  @Transactional
  public RewardTransaction handle(GrantQuestRewardCommand command) {
    // Gem credits require Monetization's durable delivery contract, which is not available yet.
    if (command.baseReward().gems() != 0) {
      throw new IllegalArgumentException("Gem rewards are not supported until Monetization is integrated");
    }
    // Serializes first and later grants for the same user, including repeated deliveries.
    var progress = progressRepository.lockForReward(command.userId());
    var prior = rewardRepository.findByOrigin(
        RewardSourceType.QUEST, command.sourceExecutionId(), command.userId());
    if (prior.isPresent()) {
      return prior.get();
    }
    var transaction = new RewardTransaction(
        UUID.randomUUID(), RewardSourceType.QUEST, command.sourceExecutionId(),
        command.userId(), command.baseReward(), command.baseReward(), command.occurredAt());
    progress.applyReward(
        transaction.grantedReward(), command.activityDate(), command.countsForDailyStreak());
    rewardRepository.save(transaction);
    progressRepository.save(progress);
    return transaction;
  }
}
