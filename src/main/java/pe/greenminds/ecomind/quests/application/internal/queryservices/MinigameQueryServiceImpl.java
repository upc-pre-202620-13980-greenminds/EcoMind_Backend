package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.MinigameQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.queries.GetAllMinigamesQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetMinigameByIdQuery;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MinigameQueryServiceImpl implements MinigameQueryService {
    private final MinigameRepository minigameRepository;

    public MinigameQueryServiceImpl(MinigameRepository minigameRepository) {
        this.minigameRepository = minigameRepository;
    }

    @Override
    public List<Minigame> handle(GetAllMinigamesQuery query) {
        return minigameRepository.findAll();
    }

    @Override
    public Optional<Minigame> handle(GetMinigameByIdQuery query) {
        return minigameRepository.findById(query.id());
    }
}
