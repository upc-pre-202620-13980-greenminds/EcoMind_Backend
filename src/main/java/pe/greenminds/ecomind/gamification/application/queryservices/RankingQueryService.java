package pe.greenminds.ecomind.gamification.application.queryservices;

import pe.greenminds.ecomind.gamification.domain.model.queries.GetRankingParticipantsQuery;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetRankingTypesQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.util.List;

public interface RankingQueryService {
    default List<RankingType> handle(GetRankingTypesQuery q) {
        return types();
    }

    default RankingPage<RankingEntry> handle(GetRankingParticipantsQuery q) {
        return participants(q.type(), q.requestedBy(), q.page(), q.size());
    }

    List<RankingType> types();

    RankingPage<RankingEntry> participants(RankingType type, UserId requester, int page, int size);

    RankingPage<RankingTransaction> transactions(
            RankingType type, UserId requester, RankingPeriod period, int page, int size);
}
