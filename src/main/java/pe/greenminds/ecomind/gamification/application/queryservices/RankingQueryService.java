package pe.greenminds.ecomind.gamification.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public interface RankingQueryService {
  List<RankingType> types();
  RankingPage<RankingEntry> participants(RankingType type, UserId requester, int page, int size);
  RankingPage<RankingTransaction> transactions(RankingType type, UserId requester,
      RankingPeriod period, int page, int size);
}
