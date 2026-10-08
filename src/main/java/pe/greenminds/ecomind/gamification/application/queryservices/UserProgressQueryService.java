package pe.greenminds.ecomind.gamification.application.queryservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetUserProgressQuery;

public interface UserProgressQueryService {
    UserProgress handle(GetUserProgressQuery query);
}
