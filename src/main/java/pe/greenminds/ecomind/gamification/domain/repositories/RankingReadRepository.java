package pe.greenminds.ecomind.gamification.domain.repositories;

import java.util.List;
import java.util.Map;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;

public interface RankingReadRepository {
  Map<Long, Long> totals(RankingType type, List<Long> beneficiaryIds);
  List<RankingTransaction> transactions(RankingType type, List<Long> beneficiaryIds,
      RankingPeriod period, int offset, int limit);
}
