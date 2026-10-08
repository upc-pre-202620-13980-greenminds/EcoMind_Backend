package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.QuestCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.ArchiveQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.PublishQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class QuestCommandServiceImpl implements QuestCommandService {

    private final QuestRepository questRepository;
    private final ActivityRepository activityRepository;

    public QuestCommandServiceImpl(
            QuestRepository questRepository,
            ActivityRepository activityRepository
    ) {
        this.questRepository = questRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    @Override
    public Result<Quest, ApplicationError> handle(CreateQuestCommand command) {
        try {
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

            var savedQuest = questRepository.save(quest);
            // The persistence assembler already restores the canonical group for a first version.
            if (savedQuest.getVersionGroupId() == null)
                savedQuest.initializeVersionGroup(savedQuest.getId());
            return Result.success(questRepository.save(savedQuest));
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
    public Result<Quest, ApplicationError> handle(ArchiveQuestCommand command) {
        var quest = questRepository.findById(command.questId());

        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", command.questId().toString())
            );
        }

        try {
            quest.get().archive();
            return Result.success(questRepository.save(quest.get()));
        } catch (IllegalStateException exception) {
            return Result.failure(ApplicationError.conflict("Quest", exception.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Quest, ApplicationError> handle(PublishQuestCommand command) {
        var quest = questRepository.findById(command.questId());
        if (quest.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Quest", command.questId().toString()));
        }
        if (questRepository.findPublishedByVersionGroupId(quest.get().getVersionGroupId()).isPresent()) {
            return Result.failure(ApplicationError.conflict(
                    "Quest", "The version group already has a published quest"));
        }
        try {
            quest.get().publish();
            return Result.success(questRepository.save(quest.get()));
        } catch (IllegalStateException exception) {
            return Result.failure(ApplicationError.conflict("Quest", exception.getMessage()));
        }
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
            if (quest.get().getPublicationStatus()
                    == pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus.PUBLISHED) {
                var originalActivities = activityRepository.findByQuestsIdOrderByOrderAsc(command.questId());
                var nextDraft = quest.get().createNextDraft(
                        command.minigameId(), command.title(), command.category(), command.description(),
                        command.type(), command.age(), new Reward(command.gemReward(), command.ecopoints()),
                        command.time(), command.image(), command.theme(), command.assignedDate());
                quest.get().archive();
                questRepository.save(quest.get());
                var savedDraft = questRepository.save(nextDraft);
                for (var activity : originalActivities) {
                    activityRepository.save(new pe.greenminds.ecomind.quests.domain.model.aggregates.Activity(
                            savedDraft.getId(), activity.getDescription(), activity.getOrder(),
                            activity.getActivityType(), activity.getActivityConfiguration(), activity.getImage()));
                }
                return Result.success(savedDraft);
            }

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
        } catch (IllegalStateException exception) {
            return Result.failure(ApplicationError.conflict("Quest", exception.getMessage()));
        }
    }
}
