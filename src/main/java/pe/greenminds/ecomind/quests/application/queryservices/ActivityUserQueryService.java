package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUserByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUserByQuestUserIdAndActivityIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUsersByQuestUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface ActivityUserQueryService {
    Optional<ActivityUser> handle(GetActivityUserByIdQuery query);

    Optional<ActivityUser> handle(GetActivityUserByQuestUserIdAndActivityIdQuery query);

    List<ActivityUser> handle(GetActivityUsersByQuestUserIdQuery query);
}
