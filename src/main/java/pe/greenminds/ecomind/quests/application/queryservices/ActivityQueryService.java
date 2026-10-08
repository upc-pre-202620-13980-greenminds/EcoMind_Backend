package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface ActivityQueryService {
    Optional<Activity> handle(GetActivityByIdQuery query);

    List<Activity> handle(GetActivitiesByQuestIdQuery query);
}
