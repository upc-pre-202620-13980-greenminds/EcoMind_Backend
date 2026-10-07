package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.application.commandservices.CollabQuestSessionCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeletePendingCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.StartCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.List;
import java.util.Objects;

@Service
public class CollabQuestSessionCommandServiceImpl implements CollabQuestSessionCommandService {
    private static final List<CollabQuestStatus> ACTIVE_SESSION_STATUSES =
            List.of(CollabQuestStatus.PENDING, CollabQuestStatus.STARTED);
    private static final List<QuestStatus> ACTIVE_QUEST_USER_STATUSES =
            List.of(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE);

    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final QuestRepository questRepository;
    private final QuestUserRepository questUserRepository;
    private final ActivityRepository activityRepository;
    private final ActivityUserRepository activityUserRepository;
    private final FamilyPlanItemRepository familyPlanItemRepository;

    public CollabQuestSessionCommandServiceImpl(
            CollabQuestSessionRepository collabQuestSessionRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            QuestRepository questRepository,
            QuestUserRepository questUserRepository,
            ActivityRepository activityRepository,
            ActivityUserRepository activityUserRepository,
            FamilyPlanItemRepository familyPlanItemRepository
    ) {
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.questRepository = questRepository;
        this.questUserRepository = questUserRepository;
        this.activityRepository = activityRepository;
        this.activityUserRepository = activityUserRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
    }

    @Transactional
    @Override
    public Result<CollabQuestSession, ApplicationError> handle(
            CreateCollabQuestSessionCommand command
    ) {
        var quest = questRepository.findById(command.questId());
        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", command.questId().toString())
            );
        }

        if (quest.get().getType() != QuestType.COLLABORATIVE) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only collaborative quests can have collaborative sessions",
                            "Quest %d is not collaborative".formatted(command.questId())
                    )
            );
        }

        if (activityRepository.countByQuestId(command.questId()) < 1) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Collaborative quest must have at least one activity",
                            "Quest %d has no activities".formatted(command.questId())
                    )
            );
        }

        if (collabQuestSessionRepository
                .findByQuestIdAndOwnerUserIdAndStatusIn(
                        command.questId(),
                        command.ownerUserId(),
                        ACTIVE_SESSION_STATUSES
                )
                .isPresent()) {
            return Result.failure(
                    ApplicationError.conflict(
                            "CollabQuestSession",
                            "The user already has a collaborative session for this quest"
                    )
            );
        }

        try {
            var collabQuestSession = new CollabQuestSession(
                    command.questId(),
                    command.ownerUserId()
            );
            var savedCollabQuestSession =
                    collabQuestSessionRepository.save(collabQuestSession);

            collabQuestMemberRepository.save(
                    new CollabQuestMember(
                            savedCollabQuestSession.getId(),
                            command.ownerUserId(),
                            command.ownerUserId(),
                            MemberRole.OWNER,
                            CollabMemberStatus.ACCEPTED
                    )
            );

            return Result.success(savedCollabQuestSession);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError(
                            "CollabQuestSession",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            return Result.failure(
                    ApplicationError.unexpected(
                            "CollabQuestSession creation",
                            exception.getMessage()
                    )
            );
        }
    }

    @Transactional
    @Override
    public Result<CollabQuestSession, ApplicationError> handle(
            StartCollabQuestSessionCommand command
    ) {
        var session = collabQuestSessionRepository.findById(command.sessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "CollabQuestSession",
                            command.sessionId().toString()
                    )
            );
        }

        if (!Objects.equals(session.get().getOwnerId(), command.ownerUserId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only the session owner can start the session",
                            "User %d is not the owner of session %d".formatted(
                                    command.ownerUserId(),
                                    command.sessionId()
                            )
                    )
            );
        }

        if (activityRepository.countByQuestId(session.get().getQuestId()) < 1) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Collaborative quest must have at least one activity",
                            "Quest %d has no activities".formatted(session.get().getQuestId())
                    )
            );
        }

        var acceptedMembers = collabQuestMemberRepository.findBySessionIdAndStatusIn(
                command.sessionId(),
                List.of(CollabMemberStatus.ACCEPTED)
        );

        if (acceptedMembers.size() < 2) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Collaborative quest requires at least two accepted members",
                            "Accepted members: %d".formatted(acceptedMembers.size())
                    )
            );
        }

        for (var member : acceptedMembers) {
            if (questUserRepository.existsByUserIdAndQuestIdAndStatusIn(
                    member.getUserId(),
                    session.get().getQuestId(),
                    ACTIVE_QUEST_USER_STATUSES
            )) {
                return Result.failure(
                        ApplicationError.conflict(
                                "QuestUser",
                                "User %d already has this quest assigned".formatted(
                                        member.getUserId()
                                )
                        )
                );
            }
        }

        try {
            for (var member : acceptedMembers) {
                var questUser = questUserRepository.save(
                        new QuestUser(
                                member.getUserId(),
                                session.get().getQuestId(),
                                session.get().getId()
                        )
                );

                var activities = activityRepository.findByQuestsIdOrderByOrderAsc(
                        questUser.getQuestId()
                );
                for (var activity : activities) {
                    activityUserRepository.save(
                            new ActivityUser(
                                    questUser.getId(),
                                    activity.getId(),
                                    activity.getDescription(),
                                    activity.getActivityConfiguration(),
                                    session.get().getId()
                            )
                    );
                }
            }

            session.get().start();
            return Result.success(collabQuestSessionRepository.save(session.get()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError(
                            "CollabQuestSession",
                            exception.getMessage()
                    )
            );
        } catch (IllegalStateException exception) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Collaborative quest session cannot be started",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            return Result.failure(
                    ApplicationError.unexpected(
                            "CollabQuestSession start",
                            exception.getMessage()
                    )
            );
        }
    }

    @Transactional
    @Override
    public Result<CollabQuestSession, ApplicationError> handle(
            DeletePendingCollabQuestSessionCommand command
    ) {
        var session = collabQuestSessionRepository.findById(command.sessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "CollabQuestSession",
                            command.sessionId().toString()
                    )
            );
        }

        if (!Objects.equals(session.get().getOwnerId(), command.ownerUserId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only the session owner can delete the session",
                            "User %d is not the owner of session %d".formatted(
                                    command.ownerUserId(),
                                    command.sessionId()
                            )
                    )
            );
        }
        if (familyPlanItemRepository.existsByCollaborativeSessionId(command.sessionId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Family plan sessions cannot be deleted directly",
                            "Cancel the family plan instead"
                    )
            );
        }

        if (session.get().getStatus() != pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending sessions can be deleted",
                            "Session %d is %s".formatted(
                                    command.sessionId(),
                                    session.get().getStatus()
                            )
                    )
            );
        }

        collabQuestMemberRepository.deleteBySessionId(command.sessionId());
        collabQuestSessionRepository.deleteById(command.sessionId());
        return Result.success(session.get());
    }
}
