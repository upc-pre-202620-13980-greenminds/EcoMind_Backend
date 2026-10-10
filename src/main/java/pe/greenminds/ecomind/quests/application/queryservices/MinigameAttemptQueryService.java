package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptsByUserAndMinigameQuery;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptByIdQuery;

public interface MinigameAttemptQueryService {
    Optional<MinigameAttempt> handle(GetMinigameAttemptByIdQuery query);

    List<MinigameAttempt> handle(GetMinigameAttemptsByUserAndMinigameQuery query);
}
