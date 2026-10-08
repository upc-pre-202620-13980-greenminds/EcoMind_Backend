package pe.greenminds.ecomind.gamification.domain.model.queries;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardBeneficiary;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.util.Objects;

public record GetRewardTransactionsQuery(
        RewardBeneficiary beneficiary,
        UserId requestedBy,
        RankingPeriod period,
        int page,
        int size) {
    public GetRewardTransactionsQuery {
        Objects.requireNonNull(beneficiary);
        Objects.requireNonNull(requestedBy);
        Objects.requireNonNull(period);
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid pagination");
    }
}
