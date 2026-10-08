package pe.greenminds.ecomind.gamification.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.outboundservices.UsersServiceClient;
import pe.greenminds.ecomind.gamification.application.queryservices.RewardQueryService;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetRewardTransactionsQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.BeneficiaryType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardHistoryEntry;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardHistoryRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
@Transactional(readOnly = true)
public class RewardQueryServiceImpl implements RewardQueryService {
    private final RewardHistoryRepository rewards;
    private final UsersServiceClient users;

    public RewardQueryServiceImpl(RewardHistoryRepository rewards, UsersServiceClient users) {
        this.rewards = rewards;
        this.users = users;
    }

    public Result<RankingPage<RewardHistoryEntry>, ApplicationError> handle(
            GetRewardTransactionsQuery q) {
        boolean allowed =
                q.beneficiary().type() == BeneficiaryType.USER
                        ? q.beneficiary().id().equals(q.requestedBy().value())
                        : users.isFamilyMember(new FamilyId(q.beneficiary().id()), q.requestedBy());
        if (!allowed)
            return Result.failure(
                    ApplicationError.forbidden(
                            "REWARD_HISTORY_FORBIDDEN",
                            "Own user history or current family membership is required"));
        var rows = rewards.find(q.beneficiary(), q.period(), q.page() * q.size(), q.size() + 1);
        boolean next = rows.size() > q.size();
        return Result.success(
                new RankingPage<>(
                        next ? rows.subList(0, q.size()) : rows, q.page(), q.size(), next));
    }
}
