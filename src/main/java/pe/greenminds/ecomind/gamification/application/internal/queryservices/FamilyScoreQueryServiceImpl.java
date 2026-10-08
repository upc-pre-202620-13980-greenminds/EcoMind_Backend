package pe.greenminds.ecomind.gamification.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.outboundservices.UsersServiceClient;
import pe.greenminds.ecomind.gamification.application.queryservices.FamilyScoreQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;
import pe.greenminds.ecomind.gamification.domain.model.entities.FamilyRewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyRewardTransactionRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyScoreRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FamilyScoreQueryServiceImpl implements FamilyScoreQueryService {
    private final FamilyScoreRepository scores;
    private final FamilyRewardTransactionRepository rewards;
    private final UsersServiceClient users;

    public FamilyScoreQueryServiceImpl(
            FamilyScoreRepository scores,
            FamilyRewardTransactionRepository rewards,
            UsersServiceClient users) {
        this.scores = scores;
        this.rewards = rewards;
        this.users = users;
    }

    public Result<FamilyScore, ApplicationError> getScore(FamilyId id, UserId requestedBy) {
        if (!users.isFamilyMember(id, requestedBy)) return denied();
        return Result.success(scores.findByFamilyId(id).orElseGet(() -> new FamilyScore(id, 0)));
    }

    public Result<List<FamilyRewardTransaction>, ApplicationError> getRewards(
            FamilyId id, UserId requestedBy) {
        if (!users.isFamilyMember(id, requestedBy)) return denied();
        return Result.success(rewards.findRecent(id));
    }

    private static <T> Result<T, ApplicationError> denied() {
        return Result.failure(
                ApplicationError.forbidden(
                        "FAMILY_SCORE_ACCESS_FORBIDDEN",
                        "Only current family members can access the family score and rewards."));
    }
}
