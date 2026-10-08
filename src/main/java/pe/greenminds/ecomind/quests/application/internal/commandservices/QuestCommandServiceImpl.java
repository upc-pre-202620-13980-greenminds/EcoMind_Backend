package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.QuestCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class QuestCommandServiceImpl implements QuestCommandService {

    private final QuestRepository questRepository;
    private final QuestUserRepository questUserRepository;
    private final ActivityRepository activityRepository;
    private final ActivityUserRepository activityUserRepository;
    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;

    public QuestCommandServiceImpl(
            QuestRepository questRepository,
            QuestUserRepository questUserRepository,
            ActivityRepository activityRepository,
            ActivityUserRepository activityUserRepository,
            CollabQuestSessionRepository collabQuestSessionRepository,
            CollabQuestMemberRepository collabQuestMemberRepository
    ) {
        this.questRepository = questRepository;
        this.questUserRepository = questUserRepository;
        this.activityRepository = activityRepository;
        this.activityUserRepository = activityUserRepository;
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
    }

    @Override
    public Result<Quest, ApplicationError> handle(CreateQuestCommand command) {
        try {
            var reward = new Reward(
                    command.reward_gems(),
                    command.reward_ecopoints()
            );

            var quest = new Quest(
                command.minimageId(),
                command.title(),
                command.category(),
                command.description(),
                command.type(),
                command.age(),
                command.reward_gems(),
                command.reward_ecopoints(),
                command.time(),
                command.image(),
                command.theme(),
                command.assignedDate()
            );

            return Result.success(questRepository.save(quest));
        } catch (IllegalArgumentException e) {
            return Result.failure(
                    ApplicationError.validationError("Quest", e.getMessage())
            );
        } catch (Exception e) {
            return Result.failure(
                    ApplicationError.unexpected("Quest creation", e.getMessage())
            );
        }
    }

    @Transactional
    @Override
    public Result<Quest, ApplicationError> handle(DeleteQuestCommand command) {
        var quest = questRepository.findById(command.questId());

        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", command.questId().toString())
            );
        }

        var questUsers = questUserRepository.findByQuestId(command.questId());
        questUsers.forEach(
                questUser -> activityUserRepository.deleteByQuestUserId(questUser.getId())
        );

        var collabQuestSessions = collabQuestSessionRepository.findByQuestId(command.questId());
        collabQuestSessions.forEach(
                session -> collabQuestMemberRepository.deleteBySessionId(session.getId())
        );
        collabQuestSessionRepository.deleteByQuestId(command.questId());

        questUserRepository.deleteByQuestId(command.questId());
        activityRepository.deleteByQuestId(command.questId());
        questRepository.deleteById(command.questId());

        return Result.success(quest.get());
    }

    @Transactional
    @Override
    public Result<Quest, ApplicationError> handle(UpdateQuestCommand command) {
        var quest = questRepository.findById(command.questId());

        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", command.questId().toString())
            );
        }

        try {
            quest.get().update(
                    command.minigameId(),
                    command.title(),
                    command.category(),
                    command.description(),
                    command.type(),
                    command.age(),
                    new Reward(command.gemReward(), command.ecopoints()),
                    command.time(),
                    command.image(),
                    command.theme(),
                    command.assignedDate()
            );

            return Result.success(questRepository.save(quest.get()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("Quest", exception.getMessage())
            );
        }
    }
}
