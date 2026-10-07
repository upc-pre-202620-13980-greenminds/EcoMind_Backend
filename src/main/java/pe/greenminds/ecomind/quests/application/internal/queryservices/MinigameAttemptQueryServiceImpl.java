package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.MinigameAttemptQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptsByUserAndMinigameQuery;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;

import java.util.List;

@Service
public class MinigameAttemptQueryServiceImpl implements MinigameAttemptQueryService {
    private final MinigameAttemptRepository minigameAttemptRepository;

    public MinigameAttemptQueryServiceImpl(MinigameAttemptRepository minigameAttemptRepository) {
        this.minigameAttemptRepository = minigameAttemptRepository;
    }

    @Override
    public List<MinigameAttempt> handle(GetMinigameAttemptsByUserAndMinigameQuery query) {
        return minigameAttemptRepository.findByUserIdAndMinigameId(
                query.userId(),
                query.minigameId()
        );
    }
}
