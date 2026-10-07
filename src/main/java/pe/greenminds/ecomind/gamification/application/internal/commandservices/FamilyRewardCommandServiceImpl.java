package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.UsersServiceClient;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyScoreRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyRewardTransactionRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class FamilyRewardCommandServiceImpl implements FamilyRewardCommandService {
  private final FamilyScoreRepository scores;
  private final FamilyRewardTransactionRepository rewards;
  private final UsersServiceClient users;

  public FamilyRewardCommandServiceImpl(FamilyScoreRepository scores,
      FamilyRewardTransactionRepository rewards, UsersServiceClient users) {
    this.scores = scores;
    this.rewards = rewards;
    this.users = users;
  }

  @Override
  @Transactional
  public Result<FamilyRewardTransaction, ApplicationError> handle(GrantFamilyPlanRewardCommand command) {
    if (!users.familyExists(command.familyId())) {
      return Result.failure(ApplicationError.notFound("FAMILY", command.familyId().value().toString()));
    }
    var score = scores.lockForReward(command.familyId());
    var prior = rewards.findByExecution(command.sourceExecutionId(), command.familyId());
    if (prior.isPresent()) return Result.success(prior.get());
    var transaction = new FamilyRewardTransaction(UUID.randomUUID(), command.sourceExecutionId(),
        command.familyId(), command.ecopoints(), command.occurredAt());
    score.addReward(transaction.ecopoints());
    rewards.save(transaction);
    scores.save(score);
    return Result.success(transaction);
  }
}
