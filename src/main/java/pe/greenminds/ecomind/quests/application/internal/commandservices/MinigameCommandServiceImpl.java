package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.MinigameCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateMinigameCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteMinigameCommand;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.Map;

@Service
public class MinigameCommandServiceImpl implements MinigameCommandService {
    private static final String MIN_SCORE_RULE = "minScore";

    private final MinigameRepository minigameRepository;
    private final MinigameAttemptRepository minigameAttemptRepository;
    private final QuestRepository questRepository;

    public MinigameCommandServiceImpl(
            MinigameRepository minigameRepository,
            MinigameAttemptRepository minigameAttemptRepository,
            QuestRepository questRepository
    ) {
        this.minigameRepository = minigameRepository;
        this.minigameAttemptRepository = minigameAttemptRepository;
        this.questRepository = questRepository;
    }

    @Override
    public Result<Minigame, ApplicationError> handle(CreateMinigameCommand command) {
        try {
            validateCompletionRules(command.completionRules());
            return Result.success(
                    minigameRepository.save(
                            new Minigame(
                                    null,
                                    command.name(),
                                    command.description(),
                                    command.url(),
                                    command.completionRules()
                            )
                    )
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("Minigame", exception.getMessage())
            );
        } catch (Exception exception) {
            return Result.failure(
                    ApplicationError.unexpected("Minigame creation", exception.getMessage())
            );
        }
    }

    @Transactional
    @Override
    public Result<Minigame, ApplicationError> handle(DeleteMinigameCommand command) {
        var minigame = minigameRepository.findById(command.minigameId());
        if (minigame.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Minigame", command.minigameId().toString())
            );
        }

        if (questRepository.existsByMinigameId(command.minigameId())) {
            return Result.failure(
                    ApplicationError.conflict(
                            "Minigame",
                            "Delete the quests associated with this minigame first"
                    )
            );
        }

        minigameAttemptRepository.deleteByMinigameId(command.minigameId());
        minigameRepository.deleteById(command.minigameId());

        return Result.success(minigame.get());
    }

    private void validateCompletionRules(Map<String, Object> completionRules) {
        if (completionRules == null || !(completionRules.get(MIN_SCORE_RULE) instanceof Number)) {
            throw new IllegalArgumentException("completionRules.minScore must be numeric");
        }
    }
}
