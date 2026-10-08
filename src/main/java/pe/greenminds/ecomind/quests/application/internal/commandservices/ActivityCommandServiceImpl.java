package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.ActivityCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateActivityCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteActivityCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateActivityCommand;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.Comparator;

@Service
public class ActivityCommandServiceImpl implements ActivityCommandService {
    private final ActivityRepository activityRepository;
    private final QuestRepository questRepository;
    private final ActivityUserRepository activityUserRepository;

    public ActivityCommandServiceImpl(
            final ActivityRepository activityRepository,
            final QuestRepository questRepository,
            final ActivityUserRepository activityUserRepository
    ) {
        this.activityRepository = activityRepository;
        this.questRepository = questRepository;
        this.activityUserRepository = activityUserRepository;
    }

    @Override
    @Transactional
    public Result<Activity, ApplicationError> handle(CreateActivityCommand command){
        var quest = questRepository.findById(command.questId());
        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", command.questId().toString())
            );
        }
        if (quest.get().getPublicationStatus() != QuestPublicationStatus.DRAFT) {
            return immutableQuestActivitiesError();
        }
        try{
            var activities = activityRepository.findByQuestsIdOrderByOrderAsc(command.questId());
            int finalOrder = Math.min(activities.size() +1, command.order());

            //Desplazar las existentes en la quest
            activities.stream()
                    .filter(activity -> activity.getOrder() >= finalOrder)
                    .sorted(Comparator.comparing(Activity::getOrder).reversed())
                    .forEach(activity -> {
                        activity.setOrder(activity.getOrder() + 1);
                        activityRepository.save(activity);
                    });

            var activity = new Activity(
                    command.questId(),
                    command.description(),
                    finalOrder,
                    command.type(),
                    command.activityConfiguration(),
                    command.image()
            );
            var savedActivity = activityRepository.save(activity);

            return Result.success(savedActivity);
        } catch(IllegalArgumentException e){
            return Result.failure(ApplicationError.validationError("Activity", e.getMessage()));
        } catch(Exception e){
            return Result.failure(
                    ApplicationError.unexpected("Activity creation", e.getMessage())
            );
        }
    }

    @Override
    @Transactional
    public Result<Activity, ApplicationError> handle(DeleteActivityCommand command) {
        var activity = activityRepository.findById(command.activityId());

        if (activity.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Activity", command.activityId().toString())
            );
        }

        var quest = questRepository.findById(activity.get().getQuestId());
        if (quest.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Quest", activity.get().getQuestId().toString()));
        }
        if (quest.get().getPublicationStatus() != QuestPublicationStatus.DRAFT) {
            return immutableQuestActivitiesError();
        }

        var deletedActivity = activity.get();
        activityUserRepository.deleteByActivityId(command.activityId());
        activityRepository.deleteById(command.activityId());

        activityRepository.findByQuestsIdOrderByOrderAsc(deletedActivity.getQuestId())
                .stream()
                .filter(currentActivity ->
                        currentActivity.getOrder() > deletedActivity.getOrder()
                )
                .forEach(currentActivity -> {
                    currentActivity.setOrder(currentActivity.getOrder() - 1);
                    activityRepository.save(currentActivity);
                });

        return Result.success(deletedActivity);
    }

    @Override
    @Transactional
    public Result<Activity, ApplicationError> handle(UpdateActivityCommand command) {
        var activity = activityRepository.findById(command.activityId());

        if (activity.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Activity", command.activityId().toString())
            );
        }

        var quest = questRepository.findById(activity.get().getQuestId());
        if (quest.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Quest", activity.get().getQuestId().toString()));
        }
        if (quest.get().getPublicationStatus() != QuestPublicationStatus.DRAFT) {
            return immutableQuestActivitiesError();
        }

        if (command.order() == null || command.order() < 1) {
            return Result.failure(
                    ApplicationError.validationError(
                            "Activity",
                            "Order must be more or equal to 1"
                    )
            );
        }

        if (command.type() == null) {
            return Result.failure(
                    ApplicationError.validationError(
                            "Activity",
                            "ActivityType is required"
                    )
            );
        }

        if (activity.get().getActivityType() != command.type()) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Activity type is immutable",
                            "Delete the activity and create a new one to change its type"
                    )
            );
        }

        try {
            var currentActivity = activity.get();
            var activities = activityRepository
                    .findByQuestsIdOrderByOrderAsc(currentActivity.getQuestId());
            int oldOrder = currentActivity.getOrder();
            int finalOrder = Math.min(activities.size(), command.order());

            if (finalOrder < oldOrder) {
                activities.stream()
                        .filter(item -> !item.getId().equals(currentActivity.getId()))
                        .filter(item ->
                                item.getOrder() >= finalOrder && item.getOrder() < oldOrder
                        )
                        .sorted(Comparator.comparing(Activity::getOrder).reversed())
                        .forEach(item -> {
                            item.setOrder(item.getOrder() + 1);
                            activityRepository.save(item);
                        });
            } else if (finalOrder > oldOrder) {
                activities.stream()
                        .filter(item -> !item.getId().equals(currentActivity.getId()))
                        .filter(item ->
                                item.getOrder() > oldOrder && item.getOrder() <= finalOrder
                        )
                        .sorted(Comparator.comparing(Activity::getOrder))
                        .forEach(item -> {
                            item.setOrder(item.getOrder() - 1);
                            activityRepository.save(item);
                        });
            }

            currentActivity.update(
                    command.description(),
                    finalOrder,
                    command.type(),
                    command.activityConfiguration(),
                    command.image()
            );

            return Result.success(activityRepository.save(currentActivity));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("Activity", exception.getMessage())
            );
        }
    }

    private Result<Activity, ApplicationError> immutableQuestActivitiesError() {
        return Result.failure(ApplicationError.conflict(
                "Quest", "Activities can only be changed while the quest is DRAFT"));
    }
}
