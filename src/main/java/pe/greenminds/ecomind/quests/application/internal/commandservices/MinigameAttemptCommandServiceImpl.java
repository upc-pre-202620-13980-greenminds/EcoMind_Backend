package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.MinigameAttemptCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.commands.CancelMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.FinishMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.Map;

@Service
public class MinigameAttemptCommandServiceImpl implements MinigameAttemptCommandService {
    private static final String MIN_SCORE_RULE = "minScore";

    private final MinigameAttemptRepository minigameAttemptRepository;
    private final MinigameRepository minigameRepository;
    private final QuestRepository questRepository;

    public MinigameAttemptCommandServiceImpl(
            MinigameAttemptRepository minigameAttemptRepository,
            MinigameRepository minigameRepository,
            QuestRepository questRepository
    ) {
        this.minigameAttemptRepository = minigameAttemptRepository;
        this.minigameRepository = minigameRepository;
        this.questRepository = questRepository;
    }

    @Override
    public Result<MinigameAttempt, ApplicationError> handle(
            CreateMinigameAttemptCommand command
    ) {
        var quest = questRepository.findById(command.questId());
        if (quest.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Quest", command.questId().toString()));
        }

        if (quest.get().getType() != QuestType.MINIGAME) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Quest must be a minigame quest",
                            "Quest %d is %s".formatted(command.questId(), quest.get().getType())
                    )
            );
        }

        if (!quest.get().acceptsNewAssignments()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Quest is not published",
                    "Only PUBLISHED quests accept new minigame attempts"));
        }

        if (quest.get().getMinigameId() == null) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Minigame quest must reference a minigame",
                            "Quest %d has no minigameId".formatted(command.questId())
                    )
            );
        }

        if (!minigameRepository.existsById(quest.get().getMinigameId())) {
            return Result.failure(
                    ApplicationError.notFound("Minigame", quest.get().getMinigameId().toString())
            );
        }

        if (minigameAttemptRepository.existsByUserIdAndStatus(
                command.userId(),
                MinigameAttemptStatus.STARTED
        )) {
            return Result.failure(
                    ApplicationError.conflict(
                            "MinigameAttempt",
                            "The user already has a started minigame attempt"
                    )
            );
        }

        try {
            return Result.success(
                    minigameAttemptRepository.save(
                            new MinigameAttempt(
                                    command.userId(),
                                    command.questId(),
                                    quest.get().getMinigameId()
                            )
                    )
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("MinigameAttempt", exception.getMessage())
            );
        } catch (Exception exception) {
            return Result.failure(
                    ApplicationError.unexpected("MinigameAttempt creation", exception.getMessage())
            );
        }
    }

    @Transactional
    @Override
    public Result<MinigameAttempt, ApplicationError> handle(
            FinishMinigameAttemptCommand command
    ) {
        var attempt = minigameAttemptRepository.findById(command.attemptId());
        if (attempt.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("MinigameAttempt", command.attemptId().toString())
            );
        }

        if (command.score() == null) {
            return Result.failure(
                    ApplicationError.validationError("score", "score must not be null")
            );
        }

        var quest = questRepository.findById(attempt.get().getQuestId());
        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", attempt.get().getQuestId().toString())
            );
        }

        var minigame = minigameRepository.findById(attempt.get().getMinigameId());
        if (minigame.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "Minigame",
                            attempt.get().getMinigameId().toString()
                    )
            );
        }

        try {
            var successful = isSuccessful(command.score(), minigame.get().getCompletionRules());
            attempt.get().finish(command.score(), command.metadata(), successful);

            var savedAttempt = minigameAttemptRepository.save(attempt.get());
            return Result.success(savedAttempt);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("MinigameAttempt", exception.getMessage())
            );
        } catch (IllegalStateException exception) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Minigame attempt cannot be finished",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            return Result.failure(
                    ApplicationError.unexpected("MinigameAttempt finish", exception.getMessage())
            );
        }
    }

    @Transactional
    @Override
    public Result<MinigameAttempt, ApplicationError> handle(
            CancelMinigameAttemptCommand command
    ) {
        var attempt = minigameAttemptRepository.findById(command.attemptId());
        if (attempt.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("MinigameAttempt", command.attemptId().toString())
            );
        }

        try {
            attempt.get().cancel();
            return Result.success(minigameAttemptRepository.save(attempt.get()));
        } catch (IllegalStateException exception) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Minigame attempt cannot be cancelled",
                            exception.getMessage()
                    )
            );
        }
    }

    private boolean isSuccessful(Integer score, Map<String, Object> completionRules) {
        var minScore = completionRules.get(MIN_SCORE_RULE);
        if (!(minScore instanceof Number minScoreNumber)) {
            throw new IllegalArgumentException("completionRules.minScore must be numeric");
        }
        return score >= minScoreNumber.intValue();
    }

}
