package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.application.commandservices.CollabQuestSessionCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeletePendingCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.StartCollabQuestSessionCommand;
import pe.greenminds.ecomind.quests.domain.model.events.CollaborativeQuestStartedEvent;
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
    private final ApplicationEventPublisher eventPublisher;

    public CollabQuestSessionCommandServiceImpl(
            CollabQuestSessionRepository collabQuestSessionRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            QuestRepository questRepository,
            QuestUserRepository questUserRepository,
            ActivityRepository activityRepository,
            ActivityUserRepository activityUserRepository,
            FamilyPlanItemRepository familyPlanItemRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.questRepository = questRepository;
        this.questUserRepository = questUserRepository;
        this.activityRepository = activityRepository;
        this.activityUserRepository = activityUserRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Override
    public Result<CollabQuestSession, ApplicationError> handle(
            CreateCollabQuestSessionCommand command
    ) {
        collabQuestSessionRepository.lockQuest(command.questId());
        var quest = questRepository.findById(command.questId());
        if (quest.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Quest", command.questId().toString())
            );
        }

        if (quest.get().getType() != QuestType.COLLABORATIVE) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_QUEST_TYPE_REQUIRED",
                            "Quest %d is not collaborative".formatted(command.questId())
                    )
            );
        }

        if (!quest.get().acceptsNewAssignments()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "COLLAB_QUEST_UNAVAILABLE",
                    "Only PUBLISHED quests accept new collaborative sessions"));
        }

        if (activityRepository.countByQuestId(command.questId()) < 1) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_NO_ACTIVITIES",
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

        if (questUserRepository.existsByUserIdAndQuestIdAndStatusIn(command.ownerUserId(), command.questId(), ACTIVE_QUEST_USER_STATUSES)
                || collabQuestMemberRepository.findByUserIdAndQuestIdAndSessionStatusIn(command.ownerUserId(), command.questId(), ACTIVE_SESSION_STATUSES)
                    .stream().anyMatch(m -> m.getStatus() == CollabMemberStatus.ACCEPTED)) {
            return Result.failure(ApplicationError.conflict("QuestUser", "User already enrolled in this quest"));
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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.validationError(
                            "CollabQuestSession",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
        collabQuestSessionRepository.lockQuestForSession(command.sessionId());
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
                    ApplicationError.forbidden("COLLAB_OWNER_REQUIRED", "Only the organizer can perform this action"
                    )
            );
        }

        if (session.get().getStatus() != pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus.PENDING) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "COLLAB_SESSION_NOT_PENDING", "The session has already started or closed"));
        }

        if (familyPlanItemRepository.existsByCollaborativeSessionId(command.sessionId())) {
            return Result.failure(ApplicationError.businessRuleViolation("COLLAB_FAMILY_PLAN_MANAGED", "Start family sessions through the family plan"));
        }

        var quest = questRepository.findById(session.get().getQuestId());
        if (quest.isEmpty() || !quest.get().acceptsNewAssignments()
                || quest.get().getType() != QuestType.COLLABORATIVE) {
            return Result.failure(ApplicationError.businessRuleViolation("COLLAB_QUEST_UNAVAILABLE", "Quest must be published and collaborative or family"));
        }

        if (activityRepository.countByQuestId(session.get().getQuestId()) < 1) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_NO_ACTIVITIES",
                            "Quest %d has no activities".formatted(session.get().getQuestId())
                    )
            );
        }

        var acceptedMembers = collabQuestMemberRepository.findBySessionIdAndStatusIn(
                command.sessionId(),
                List.of(CollabMemberStatus.ACCEPTED)
        );

        if (acceptedMembers.size() < CollabQuestSession.MIN_ACCEPTED_MEMBERS) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_MIN_MEMBERS_REQUIRED",
                            "Accepted members: %d".formatted(acceptedMembers.size())
                    )
            );
        }

        for (var member : acceptedMembers) {
            if (collabQuestMemberRepository.findByUserIdAndQuestIdAndSessionStatusIn(
                    member.getUserId(), session.get().getQuestId(), ACTIVE_SESSION_STATUSES).stream()
                    .anyMatch(m -> m.getStatus() == CollabMemberStatus.ACCEPTED
                            && !Objects.equals(m.getSessionId(), command.sessionId()))) {
                return Result.failure(ApplicationError.conflict("CollabQuestMember", "User accepted another session for this quest"));
            }
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
            var activities = activityRepository.findByQuestsIdOrderByOrderAsc(session.get().getQuestId());
            if (activities.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation("COLLAB_NO_ACTIVITIES", "Quest has no activities"));
            }
            // Validate every activity snapshot before persisting any assignment.
            for (var activity : activities) {
                new ActivityUser(1L, activity.getId(), activity.getDescription(), activity.getActivityConfiguration(), session.get().getId());
            }
            for (var member : acceptedMembers) {
                var questUser = questUserRepository.save(
                        new QuestUser(
                                member.getUserId(),
                                session.get().getQuestId(),
                                session.get().getId()
                        )
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
            var savedSession = collabQuestSessionRepository.save(session.get());
            eventPublisher.publishEvent(new CollaborativeQuestStartedEvent(
                    savedSession.getId(), savedSession.getQuestId(), savedSession.getOwnerId(),
                    java.time.OffsetDateTime.now()));
            return Result.success(savedSession);
        } catch (IllegalArgumentException | NullPointerException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.validationError(
                            "CollabQuestSession",
                            exception.getMessage()
                    )
            );
        } catch (IllegalStateException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_START_FAILED",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
        collabQuestSessionRepository.lockQuestForSession(command.sessionId());
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
                    ApplicationError.forbidden("COLLAB_OWNER_REQUIRED", "Only the organizer can perform this action"
                    )
            );
        }
        if (familyPlanItemRepository.existsByCollaborativeSessionId(command.sessionId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_FAMILY_PLAN_MANAGED",
                            "Cancel the family plan instead"
                    )
            );
        }

        if (session.get().getStatus() != pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_SESSION_NOT_PENDING",
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
