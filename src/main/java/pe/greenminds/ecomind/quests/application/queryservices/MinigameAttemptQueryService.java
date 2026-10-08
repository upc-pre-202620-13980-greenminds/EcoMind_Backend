package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptsByUserAndMinigameQuery;

import java.util.List;

public interface MinigameAttemptQueryService {
    List<MinigameAttempt> handle(GetMinigameAttemptsByUserAndMinigameQuery query);
}
