package pe.greenminds.ecomind.quests.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.quests.application.commandservices.ActivityUserCommandService;
import pe.greenminds.ecomind.quests.application.internal.services.DailyQuestLifecycleService;
import pe.greenminds.ecomind.quests.application.internal.submission.ActivitySubmissionHandlerRegistry;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateActivityUserCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.SubmitActivityUserCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class ActivityUserCommandServiceImpl implements ActivityUserCommandService {
    private static final java.util.List<QuestStatus> ACTIVE_QUEST_USER_STATUSES =
            java.util.List.of(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE);

    private final ActivityUserRepository activityUserRepository;
    private final ActivityRepository activityRepository;
    private final QuestUserRepository questUserRepository;
    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final ActivitySubmissionHandlerRegistry submissionHandlerRegistry;
    private final DailyQuestLifecycleService dailyQuestLifecycleService;

    public ActivityUserCommandServiceImpl(
            ActivityUserRepository activityUserRepository,
            ActivityRepository activityRepository,
            QuestUserRepository questUserRepository,
            CollabQuestSessionRepository collabQuestSessionRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            ActivitySubmissionHandlerRegistry submissionHandlerRegistry,
            DailyQuestLifecycleService dailyQuestLifecycleService
    ) {
        this.activityUserRepository = activityUserRepository;
        this.activityRepository = activityRepository;
        this.questUserRepository = questUserRepository;
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.submissionHandlerRegistry = submissionHandlerRegistry;
        this.dailyQuestLifecycleService = dailyQuestLifecycleService;
    }

    @Override
    public Result<ActivityUser, ApplicationError> handle(CreateActivityUserCommand command) {
        var questUser = questUserRepository.findById(command.questUserId());
        if (questUser.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("QuestUser", command.questUserId().toString())
            );
        }

        var activity = activityRepository.findById(command.activityId());
        if (activity.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Activity", command.activityId().toString())
            );
        }

        if (activityUserRepository.existsByQuestUserIdAndActivityId(
                command.questUserId(),
                command.activityId()
        )) {
            return Result.failure(
                    ApplicationError.conflict(
                            "ActivityUser",
                            "The activity is already assigned to this quest user"
                    )
            );
        }

        if (!questUser.get().getQuestId().equals(activity.get().getQuestId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Activity must belong to the assigned quest",
                            "The activity and quest user reference different quests"
                    )
            );
        }

        try {
            var activityUser = new ActivityUser(
                    command.questUserId(),
                    command.activityId(),
                    activity.get().getDescription(),
                    activity.get().getActivityConfiguration(),
                    command.collaborativeSessionId()
            );
            return Result.success(activityUserRepository.save(activityUser));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("ActivityUser", exception.getMessage())
            );
        } catch (Exception exception) {
            return Result.failure(
                    ApplicationError.unexpected("ActivityUser creation", exception.getMessage())
            );
        }
    }

    @Override
    @Transactional
    public Result<ActivityUser, ApplicationError> handle(SubmitActivityUserCommand command) {
        var activityUser = activityUserRepository.findById(command.activityUserId());
        if (activityUser.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "ActivityUser",
                            command.activityUserId().toString()
                    )
            );
        }

        var questUser = questUserRepository.findById(activityUser.get().getQuestUserId());
        if (questUser.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "QuestUser",
                            activityUser.get().getQuestUserId().toString()
                    )
            );
        }
        dailyQuestLifecycleService.expireOpenDailyQuestsForUser(questUser.get().getUserId());
        questUser = questUserRepository.findById(activityUser.get().getQuestUserId());
        if (questUser.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "QuestUser",
                            activityUser.get().getQuestUserId().toString()
                    )
            );
        }

        if (questUser.get().getStatus() == QuestStatus.COMPLETED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Completed quest attempts cannot be modified",
                            "The quest is already completed"
                    )
            );
        }

        var activity = activityRepository.findById(activityUser.get().getActivityId());
        if (activity.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "Activity",
                            activityUser.get().getActivityId().toString()
                    )
            );
        }

        var submissionHandler = submissionHandlerRegistry.findByType(
                activity.get().getActivityType()
        );
        if (submissionHandler.isEmpty()) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Unsupported activity type",
                            "Activity type: " + activity.get().getActivityType()
                    )
            );
        }

        try {
            if (activityUser.get().getCollaborativeSessionId() == null) {
                submissionHandler.get().submit(
                        activityUser.get(),
                        command.data()
                );
            } else {
                return submitCollaborativeActivity(
                        activityUser.get(),
                        questUser.get(),
                        command,
                        submissionHandler.get()
                );
            }
        } catch (IllegalArgumentException exception) {
            return Result.failure(
                    ApplicationError.validationError(
                            "Activity submission",
                            exception.getMessage()
                    )
            );
        }

        var savedActivityUser = activityUserRepository.save(activityUser.get());
        var activityUsers = activityUserRepository.findByQuestUserId(
                questUser.get().getId()
        );
        var questProgress = activityUsers.stream()
                .mapToDouble(ActivityUser::getProgress)
                .average()
                .orElse(0.0);

        questUser.get().updateProgress(questProgress);
        questUserRepository.save(questUser.get());

        return Result.success(savedActivityUser);
    }

    private Result<ActivityUser, ApplicationError> submitCollaborativeActivity(
            ActivityUser submittedActivityUser,
            QuestUser submittedQuestUser,
            SubmitActivityUserCommand command,
            pe.greenminds.ecomind.quests.application.internal.submission.ActivitySubmissionHandler submissionHandler
    ) {
        var session = collabQuestSessionRepository.findById(
                submittedActivityUser.getCollaborativeSessionId()
        );
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "CollabQuestSession",
                            submittedActivityUser.getCollaborativeSessionId().toString()
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.STARTED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Collaborative quest session must be started",
                            "Session %d is %s".formatted(
                                    session.get().getId(),
                                    session.get().getStatus()
                            )
                    )
            );
        }

        var acceptedMembers = collabQuestMemberRepository.findBySessionIdAndStatusIn(
                session.get().getId(),
                java.util.List.of(CollabMemberStatus.ACCEPTED)
        );
        var submittedUserIsAccepted = acceptedMembers.stream()
                .anyMatch(member -> member.getUserId().equals(submittedQuestUser.getUserId()));

        if (!submittedUserIsAccepted) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only accepted members can submit collaborative progress",
                            "User %d is not accepted in session %d".formatted(
                                    submittedQuestUser.getUserId(),
                                    session.get().getId()
                            )
                    )
            );
        }

        var synchronizedActivityUsers = new java.util.ArrayList<ActivityUser>();
        var synchronizedQuestUsers = new java.util.ArrayList<QuestUser>();

        for (var member : acceptedMembers) {
            var questUser = questUserRepository.findFirstByUserIdAndQuestIdAndStatusIn(
                    member.getUserId(),
                    session.get().getQuestId(),
                    ACTIVE_QUEST_USER_STATUSES
            );

            if (questUser.isEmpty()
                    || !session.get().getId().equals(
                            questUser.get().getCollaborativeSessionId()
                    )) {
                return Result.failure(
                        ApplicationError.notFound(
                                "QuestUser",
                                "userId=%d, questId=%d, collaborativeSessionId=%d".formatted(
                                        member.getUserId(),
                                        session.get().getQuestId(),
                                        session.get().getId()
                                )
                        )
                );
            }

            if (questUser.get().getStatus() == QuestStatus.COMPLETED) {
                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                "Completed quest attempts cannot be modified",
                                "QuestUser %d is already completed".formatted(
                                        questUser.get().getId()
                                )
                        )
                );
            }

            var activityUser = activityUserRepository.findByQuestUserIdAndActivityId(
                    questUser.get().getId(),
                    submittedActivityUser.getActivityId()
            );

            if (activityUser.isEmpty()
                    || !session.get().getId().equals(
                            activityUser.get().getCollaborativeSessionId()
                    )) {
                return Result.failure(
                        ApplicationError.notFound(
                                "ActivityUser",
                                "questUserId=%d, activityId=%d, collaborativeSessionId=%d"
                                        .formatted(
                                                questUser.get().getId(),
                                                submittedActivityUser.getActivityId(),
                                                session.get().getId()
                                        )
                        )
                );
            }

            synchronizedQuestUsers.add(questUser.get());
            synchronizedActivityUsers.add(activityUser.get());
        }

        try {
            for (var activityUser : synchronizedActivityUsers) {
                submissionHandler.submit(activityUser, command.data());
            }
        } catch (IllegalArgumentException exception) {
            return Result.failure(
                    ApplicationError.validationError(
                            "Activity submission",
                            exception.getMessage()
                    )
            );
        }

        ActivityUser savedSubmittedActivityUser = null;
        for (var activityUser : synchronizedActivityUsers) {
            var savedActivityUser = activityUserRepository.save(activityUser);
            if (activityUser.getId().equals(submittedActivityUser.getId())) {
                savedSubmittedActivityUser = savedActivityUser;
            }
        }

        for (var questUser : synchronizedQuestUsers) {
            updateQuestUserProgress(questUser);
        }

        return Result.success(savedSubmittedActivityUser);
    }

    private void updateQuestUserProgress(QuestUser questUser) {
        var activityUsers = activityUserRepository.findByQuestUserId(
                questUser.getId()
        );
        var questProgress = activityUsers.stream()
                .mapToDouble(ActivityUser::getProgress)
                .average()
                .orElse(0.0);

        questUser.updateProgress(questProgress);
        questUserRepository.save(questUser);
    }
}
