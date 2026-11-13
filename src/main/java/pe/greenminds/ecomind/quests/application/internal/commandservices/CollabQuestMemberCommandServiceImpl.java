package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pe.greenminds.ecomind.quests.application.commandservices.CollabQuestMemberCommandService;
import pe.greenminds.ecomind.quests.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.commands.AcceptCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeclineCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.InviteCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.LeaveCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.RemoveCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.events.CollaborativeQuestInvitationAcceptedEvent;
import pe.greenminds.ecomind.quests.domain.model.events.CollaborativeQuestInvitationRejectedEvent;
import pe.greenminds.ecomind.quests.domain.model.events.CollaborativeQuestInvitationSentEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.Objects;

@Service
@Transactional
public class CollabQuestMemberCommandServiceImpl implements CollabQuestMemberCommandService {
    private static final java.util.List<CollabQuestStatus> ACTIVE_SESSION_STATUSES =
            java.util.List.of(CollabQuestStatus.PENDING, CollabQuestStatus.STARTED);
    private static final java.util.List<QuestStatus> ACTIVE_QUEST_USER_STATUSES =
            java.util.List.of(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE);

    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final QuestUserRepository questUserRepository;
    private final pe.greenminds.ecomind.quests.domain.repositories.QuestRepository questRepository;
    private final ActivityUserRepository activityUserRepository;
    private final FamilyPlanItemRepository familyPlanItemRepository;
    private final UsersServiceClient usersServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    public CollabQuestMemberCommandServiceImpl(
            CollabQuestMemberRepository collabQuestMemberRepository,
            CollabQuestSessionRepository collabQuestSessionRepository,
            QuestUserRepository questUserRepository,
            pe.greenminds.ecomind.quests.domain.repositories.QuestRepository questRepository,
            ActivityUserRepository activityUserRepository,
            FamilyPlanItemRepository familyPlanItemRepository,
            UsersServiceClient usersServiceClient,
            ApplicationEventPublisher eventPublisher
    ) {
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.questUserRepository = questUserRepository;
        this.questRepository = questRepository;
        this.activityUserRepository = activityUserRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
        this.usersServiceClient = usersServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Result<CollabQuestMember, ApplicationError> handle(
            InviteCollabQuestMemberCommand command
    ) {
        collabQuestSessionRepository.lockQuestForSession(command.sessionId());
        var session = collabQuestSessionRepository.findById(command.sessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestSession", command.sessionId().toString())
            );
        }
        if (!Objects.equals(session.get().getOwnerId(), command.invitedByUserId())) {
            return Result.failure(ApplicationError.forbidden("COLLAB_OWNER_REQUIRED", "Only the organizer can invite"));
        }
        if (isFamilyPlanSession(command.sessionId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_FAMILY_PLAN_MANAGED",
                            "Session %d belongs to a family plan".formatted(command.sessionId())
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.PENDING) {
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

        var quest = questRepository.findById(session.get().getQuestId());
        if (quest.isEmpty() || !quest.get().acceptsNewAssignments()) {
            return Result.failure(ApplicationError.businessRuleViolation("COLLAB_QUEST_UNAVAILABLE", "Quest must be published"));
        }
        if (questUserRepository.existsByUserIdAndQuestIdAndStatusIn(command.invitedByUserId(), session.get().getQuestId(), ACTIVE_QUEST_USER_STATUSES)
                || questUserRepository.existsByUserIdAndQuestIdAndStatusIn(command.invitedUserId(), session.get().getQuestId(), ACTIVE_QUEST_USER_STATUSES)) {
            return Result.failure(ApplicationError.conflict("QuestUser", "User already has this quest assigned"));
        }

        if (Objects.equals(command.invitedByUserId(), command.invitedUserId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_SELF_INVITATION",
                            "invitedByUserId and invitedUserId must be different"
                    )
            );
        }


        if (collabQuestMemberRepository.findBySessionIdAndStatusIn(command.sessionId(),
                java.util.List.of(CollabMemberStatus.PENDING, CollabMemberStatus.ACCEPTED)).size() >= CollabQuestSession.MAX_MEMBERS) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "COLLAB_SESSION_FULL", "At most five active members are allowed"));
        }

        if (!usersServiceClient.existsUser(command.invitedUserId())) {
            return Result.failure(ApplicationError.notFound("User", command.invitedUserId().toString()));
        }
        if (!usersServiceClient.areFriends(command.invitedByUserId(), command.invitedUserId())
                && !usersServiceClient.belongToSameFamily(command.invitedByUserId(), command.invitedUserId())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "COLLAB_RELATIONSHIP_REQUIRED",
                    "Users %d and %d have no accepted social relationship".formatted(
                            command.invitedByUserId(), command.invitedUserId())));
        }

        if (collabQuestMemberRepository.existsBySessionIdAndUserId(
                command.sessionId(),
                command.invitedUserId()
        )) {
            return Result.failure(
                    ApplicationError.conflict(
                            "CollabQuestMember",
                            "The user is already invited to this session"
                    )
            );
        }

        var acceptedQuestMemberships = collabQuestMemberRepository
                .findByUserIdAndQuestIdAndSessionStatusIn(
                command.invitedUserId(),
                session.get().getQuestId(),
                ACTIVE_SESSION_STATUSES
        );
        var alreadyAcceptedQuest = acceptedQuestMemberships.stream()
                .anyMatch(member -> member.getStatus() == CollabMemberStatus.ACCEPTED);

        if (alreadyAcceptedQuest) {
            return Result.failure(
                    ApplicationError.conflict(
                            "CollabQuestMember",
                            "The user already accepted an invitation for this quest"
                    )
            );
        }

        try {
            var member = new CollabQuestMember(
                    command.sessionId(),
                    command.invitedUserId(),
                    command.invitedByUserId(),
                    MemberRole.PARTICIPANT,
                    CollabMemberStatus.PENDING
            );

            var savedMember = collabQuestMemberRepository.save(member);
            eventPublisher.publishEvent(new CollaborativeQuestInvitationSentEvent(
                    savedMember.getId(), savedMember.getSessionId(), savedMember.getUserId(),
                    savedMember.getOwnerId(), java.time.OffsetDateTime.now()));
            return Result.success(savedMember);
        } catch (IllegalArgumentException | NullPointerException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.validationError("CollabQuestMember", exception.getMessage())
            );
        } catch (Exception exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.unexpected(
                            "CollabQuestMember invitation",
                            exception.getMessage()
                    )
            );
        }
    }

    @Override
    public Result<CollabQuestMember, ApplicationError> handle(
            AcceptCollabQuestMemberCommand command
    ) {
        collabQuestSessionRepository.lockQuestForMember(command.memberId());
        var member = collabQuestMemberRepository.findById(command.memberId());
        if (member == null) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestMember", command.memberId().toString())
            );
        }
        if (!Objects.equals(member.getUserId(), command.actorUserId())) {
            return Result.failure(ApplicationError.forbidden("COLLAB_MEMBER_FORBIDDEN", "Only the recipient can answer or leave"));
        }


        if (member.getRole() == MemberRole.OWNER) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_OWNER_INVITATION",
                            "Member %d is the session owner".formatted(command.memberId())
                    )
            );
        }

        if (member.getStatus() != CollabMemberStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_INVITATION_NOT_PENDING",
                            "Member %d is %s".formatted(command.memberId(), member.getStatus())
                    )
            );
        }

        var session = collabQuestSessionRepository.findById(member.getSessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "CollabQuestSession",
                            member.getSessionId().toString()
                    )
            );
        }
        if (isFamilyPlanSession(session.get().getId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_FAMILY_PLAN_MANAGED",
                            "Session %d belongs to a family plan".formatted(session.get().getId())
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_INVITATION_INVALID",
                            "Session %d is %s".formatted(
                                    session.get().getId(),
                                    session.get().getStatus()
                            )
                    )
            );
        }

        var acceptedQuestMemberships = collabQuestMemberRepository
                .findByUserIdAndQuestIdAndSessionStatusIn(
                member.getUserId(),
                session.get().getQuestId(),
                ACTIVE_SESSION_STATUSES
        );
        var alreadyAcceptedQuest = acceptedQuestMemberships.stream()
                .anyMatch(existingMember ->
                        !Objects.equals(existingMember.getId(), member.getId())
                                && existingMember.getStatus() == CollabMemberStatus.ACCEPTED
                );

        if (alreadyAcceptedQuest) {
            return Result.failure(
                    ApplicationError.conflict(
                            "CollabQuestMember",
                            "The user already accepted an invitation for this quest"
                    )
            );
        }

        var activeQuestUser = questUserRepository.findFirstByUserIdAndQuestIdAndStatusIn(
                member.getUserId(),
                session.get().getQuestId(),
                ACTIVE_QUEST_USER_STATUSES
        );
        if (activeQuestUser.isPresent()) {
            return Result.failure(
                    ApplicationError.conflict(
                            "QuestUser",
                            "The user must finish or leave their current quest session first"
                    )
            );
        }

        try {
            member.answerInvite(CollabMemberStatus.ACCEPTED);
            var savedMember = collabQuestMemberRepository.save(member);
            eventPublisher.publishEvent(new CollaborativeQuestInvitationAcceptedEvent(
                    savedMember.getId(), savedMember.getSessionId(), savedMember.getUserId(),
                    savedMember.getOwnerId(), java.time.OffsetDateTime.now()));
            return Result.success(savedMember);
        } catch (IllegalArgumentException | NullPointerException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.validationError("CollabQuestMember", exception.getMessage())
            );
        } catch (Exception exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.unexpected(
                            "CollabQuestMember accept",
                            exception.getMessage()
                    )
            );
        }
    }

    @Override
    public Result<CollabQuestMember, ApplicationError> handle(
            DeclineCollabQuestMemberCommand command
    ) {
        collabQuestSessionRepository.lockQuestForMember(command.memberId());
        var member = collabQuestMemberRepository.findById(command.memberId());
        if (member == null) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestMember", command.memberId().toString())
            );
        }
        if (!Objects.equals(member.getUserId(), command.actorUserId())) {
            return Result.failure(ApplicationError.forbidden("COLLAB_MEMBER_FORBIDDEN", "Only the recipient can answer or leave"));
        }


        var session = collabQuestSessionRepository.findById(member.getSessionId());
        if (session.isEmpty() || session.get().getStatus() != CollabQuestStatus.PENDING) {
            return Result.failure(ApplicationError.businessRuleViolation("COLLAB_INVITATION_INVALID", "Invitation session is no longer pending"));
        }

        if (member.getRole() == MemberRole.OWNER) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_OWNER_INVITATION",
                            "Member %d is the session owner".formatted(command.memberId())
                    )
            );
        }
        if (isFamilyPlanSession(member.getSessionId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_FAMILY_PLAN_MANAGED",
                            "Session %d belongs to a family plan".formatted(member.getSessionId())
                    )
            );
        }

        if (member.getStatus() != CollabMemberStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_INVITATION_NOT_PENDING",
                            "Member %d is %s".formatted(command.memberId(), member.getStatus())
                    )
            );
        }

        try {
            member.declineInvite();
            var savedMember = collabQuestMemberRepository.save(member);
            publishInvitationRejected(savedMember);
            return Result.success(savedMember);
        } catch (IllegalArgumentException | NullPointerException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.validationError("CollabQuestMember", exception.getMessage())
            );
        } catch (Exception exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.failure(
                    ApplicationError.unexpected(
                            "CollabQuestMember decline",
                            exception.getMessage()
                    )
            );
        }
    }

    @Override
    @Transactional
    public Result<CollabQuestMember, ApplicationError> handle(
            LeaveCollabQuestMemberCommand command
    ) {
        collabQuestSessionRepository.lockQuestForMember(command.memberId());
        var member = collabQuestMemberRepository.findById(command.memberId());
        if (member == null) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestMember", command.memberId().toString())
            );
        }
        if (!Objects.equals(member.getUserId(), command.actorUserId())) {
            return Result.failure(ApplicationError.forbidden("COLLAB_MEMBER_FORBIDDEN", "Only the recipient can answer or leave"));
        }


        var session = collabQuestSessionRepository.findById(member.getSessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "CollabQuestSession",
                            member.getSessionId().toString()
                    )
            );
        }
        if (isFamilyPlanSession(session.get().getId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_FAMILY_PLAN_MANAGED",
                            "Session %d belongs to a family plan".formatted(session.get().getId())
                    )
            );
        }

        if (member.getStatus() != CollabMemberStatus.PENDING && member.getStatus() != CollabMemberStatus.ACCEPTED) {
            return Result.failure(ApplicationError.businessRuleViolation("COLLAB_MEMBERSHIP_INVALID", "Only pending or accepted members can leave"));
        }
        if (session.get().getStatus() == CollabQuestStatus.PENDING) {
            if (member.getRole() == MemberRole.OWNER) {
                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                "COLLAB_OWNER_CANNOT_LEAVE",
                                "The owner must delete the pending session instead"
                        )
                );
            }

            member.declineInvite();
            var savedMember = collabQuestMemberRepository.save(member);
            publishInvitationRejected(savedMember);
            return Result.success(savedMember);
        }

        if (session.get().getStatus() != CollabQuestStatus.STARTED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_SESSION_CLOSED",
                            "Session %d is %s".formatted(
                                    session.get().getId(),
                                    session.get().getStatus()
                            )
                    )
            );
        }

        if (member.getStatus() != CollabMemberStatus.ACCEPTED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_MEMBERSHIP_INVALID",
                            "Member %d is %s".formatted(member.getId(), member.getStatus())
                    )
            );
        }

        member.leaveQuest();
        var savedMember = collabQuestMemberRepository.save(member);
        deleteMemberProgress(member.getUserId(), session.get().getQuestId(), session.get().getId());
        cancelIfNoAcceptedMembersRemain(session.get().getId());

        return Result.success(savedMember);
    }

    @Override
    @Transactional
    public Result<CollabQuestMember, ApplicationError> handle(
            RemoveCollabQuestMemberCommand command
    ) {
        collabQuestSessionRepository.lockQuestForMember(command.memberId());
        var member = collabQuestMemberRepository.findById(command.memberId());
        if (member == null) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestMember", command.memberId().toString())
            );
        }

        var session = collabQuestSessionRepository.findById(member.getSessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "CollabQuestSession",
                            member.getSessionId().toString()
                    )
            );
        }
        if (isFamilyPlanSession(session.get().getId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_FAMILY_PLAN_MANAGED",
                            "Session %d belongs to a family plan".formatted(session.get().getId())
                    )
            );
        }

        if (!Objects.equals(session.get().getOwnerId(), command.ownerUserId())) {
            return Result.failure(
                    ApplicationError.forbidden("COLLAB_OWNER_REQUIRED", "Only the organizer can perform this action"
                    )
            );
        }

        if (member.getRole() == MemberRole.OWNER) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_OWNER_CANNOT_REMOVE_SELF",
                            "Delete the pending session instead"
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "COLLAB_SESSION_NOT_PENDING",
                            "Session %d is %s".formatted(
                                    session.get().getId(),
                                    session.get().getStatus()
                            )
                    )
            );
        }

        member.revokeInvite();
        return Result.success(collabQuestMemberRepository.save(member));
    }

    private void deleteMemberProgress(Long userId, Long questId, Long sessionId) {
        var questUser = questUserRepository.findFirstByUserIdAndQuestIdAndStatusIn(
                userId,
                questId,
                ACTIVE_QUEST_USER_STATUSES
        );
        if (questUser.isEmpty() || !Objects.equals(questUser.get().getCollaborativeSessionId(), sessionId)) {
            return;
        }

        activityUserRepository.deleteByQuestUserId(questUser.get().getId());
        questUserRepository.deleteById(questUser.get().getId());
    }

    private void cancelIfNoAcceptedMembersRemain(Long sessionId) {
        var acceptedMembers = collabQuestMemberRepository.findBySessionIdAndStatusIn(
                sessionId,
                java.util.List.of(CollabMemberStatus.ACCEPTED)
        );
        if (!acceptedMembers.isEmpty()) {
            return;
        }

        var session = collabQuestSessionRepository.findById(sessionId);
        if (session.isPresent() && session.get().getStatus() == CollabQuestStatus.STARTED) {
            session.get().cancel();
            collabQuestSessionRepository.save(session.get());
        }
    }

    private boolean isFamilyPlanSession(Long sessionId) {
        return familyPlanItemRepository.existsByCollaborativeSessionId(sessionId);
    }

    private void publishInvitationRejected(CollabQuestMember member) {
        eventPublisher.publishEvent(new CollaborativeQuestInvitationRejectedEvent(
                member.getId(), member.getSessionId(), member.getUserId(), member.getOwnerId(),
                java.time.OffsetDateTime.now()));
    }
}
