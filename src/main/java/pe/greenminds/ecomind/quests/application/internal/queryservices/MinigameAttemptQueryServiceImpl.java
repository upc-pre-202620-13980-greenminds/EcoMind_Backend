package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.MinigameAttemptQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptsByUserAndMinigameQuery;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameAttemptByIdQuery;

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
    @Override
    public Optional<MinigameAttempt> handle(GetMinigameAttemptByIdQuery query) {
        return minigameAttemptRepository.findById(query.attemptId());
    }
}
