package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.CollabQuestMemberCommandService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.commands.AcceptCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeclineCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.InviteCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.LeaveCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.RemoveCollabQuestMemberCommand;
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
public class CollabQuestMemberCommandServiceImpl implements CollabQuestMemberCommandService {
    private static final java.util.List<CollabQuestStatus> ACTIVE_SESSION_STATUSES =
            java.util.List.of(CollabQuestStatus.PENDING, CollabQuestStatus.STARTED);
    private static final java.util.List<QuestStatus> ACTIVE_QUEST_USER_STATUSES =
            java.util.List.of(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE);

    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final QuestUserRepository questUserRepository;
    private final ActivityUserRepository activityUserRepository;
    private final FamilyPlanItemRepository familyPlanItemRepository;

    public CollabQuestMemberCommandServiceImpl(
            CollabQuestMemberRepository collabQuestMemberRepository,
            CollabQuestSessionRepository collabQuestSessionRepository,
            QuestUserRepository questUserRepository,
            ActivityUserRepository activityUserRepository,
            FamilyPlanItemRepository familyPlanItemRepository
    ) {
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.questUserRepository = questUserRepository;
        this.activityUserRepository = activityUserRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
    }

    @Override
    public Result<CollabQuestMember, ApplicationError> handle(
            InviteCollabQuestMemberCommand command
    ) {
        var session = collabQuestSessionRepository.findById(command.sessionId());
        if (session.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestSession", command.sessionId().toString())
            );
        }
        if (isFamilyPlanSession(command.sessionId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Family plan sessions do not accept manual invitations",
                            "Session %d belongs to a family plan".formatted(command.sessionId())
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending sessions can receive invitations",
                            "Session %d is %s".formatted(
                                    command.sessionId(),
                                    session.get().getStatus()
                            )
                    )
            );
        }

        if (Objects.equals(command.invitedByUserId(), command.invitedUserId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "A user cannot invite themselves",
                            "invitedByUserId and invitedUserId must be different"
                    )
            );
        }

        if (!Objects.equals(session.get().getOwnerId(), command.invitedByUserId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only the session owner can invite users",
                            "User %d is not the owner of session %d".formatted(
                                    command.invitedByUserId(),
                                    command.sessionId()
                            )
                    )
            );
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

            return Result.success(collabQuestMemberRepository.save(member));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("CollabQuestMember", exception.getMessage())
            );
        } catch (Exception exception) {
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
        var member = collabQuestMemberRepository.findById(command.memberId());
        if (member == null) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestMember", command.memberId().toString())
            );
        }

        if (member.getRole() == MemberRole.OWNER) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Owner cannot accept invitations",
                            "Member %d is the session owner".formatted(command.memberId())
                    )
            );
        }

        if (member.getStatus() != CollabMemberStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending invitations can be accepted",
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
                            "Family plan invitations cannot be accepted manually",
                            "Session %d belongs to a family plan".formatted(session.get().getId())
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending sessions can accept invitations",
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
            return Result.success(collabQuestMemberRepository.save(member));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("CollabQuestMember", exception.getMessage())
            );
        } catch (Exception exception) {
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
        var member = collabQuestMemberRepository.findById(command.memberId());
        if (member == null) {
            return Result.failure(
                    ApplicationError.notFound("CollabQuestMember", command.memberId().toString())
            );
        }

        if (member.getRole() == MemberRole.OWNER) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Owner cannot decline invitations",
                            "Member %d is the session owner".formatted(command.memberId())
                    )
            );
        }
        if (isFamilyPlanSession(member.getSessionId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Family plan invitations cannot be declined manually",
                            "Session %d belongs to a family plan".formatted(member.getSessionId())
                    )
            );
        }

        if (member.getStatus() != CollabMemberStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending invitations can be declined",
                            "Member %d is %s".formatted(command.memberId(), member.getStatus())
                    )
            );
        }

        try {
            member.declineInvite();
            return Result.success(collabQuestMemberRepository.save(member));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(
                    ApplicationError.validationError("CollabQuestMember", exception.getMessage())
            );
        } catch (Exception exception) {
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
                            "Family plan members cannot leave individually",
                            "Session %d belongs to a family plan".formatted(session.get().getId())
                    )
            );
        }

        if (session.get().getStatus() == CollabQuestStatus.PENDING) {
            if (member.getRole() == MemberRole.OWNER) {
                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                "Owner cannot leave a pending session",
                                "The owner must delete the pending session instead"
                        )
                );
            }

            member.declineInvite();
            return Result.success(collabQuestMemberRepository.save(member));
        }

        if (session.get().getStatus() != CollabQuestStatus.STARTED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending or started session members can leave",
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
                            "Only accepted members can leave started sessions",
                            "Member %d is %s".formatted(member.getId(), member.getStatus())
                    )
            );
        }

        member.leaveQuest();
        var savedMember = collabQuestMemberRepository.save(member);
        deleteMemberProgress(member.getUserId(), session.get().getQuestId());
        cancelIfNoAcceptedMembersRemain(session.get().getId());

        return Result.success(savedMember);
    }

    @Override
    @Transactional
    public Result<CollabQuestMember, ApplicationError> handle(
            RemoveCollabQuestMemberCommand command
    ) {
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
                            "Family plan members cannot be removed individually",
                            "Session %d belongs to a family plan".formatted(session.get().getId())
                    )
            );
        }

        if (!Objects.equals(session.get().getOwnerId(), command.ownerUserId())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only the session owner can remove members",
                            "User %d is not the owner of session %d".formatted(
                                    command.ownerUserId(),
                                    session.get().getId()
                            )
                    )
            );
        }

        if (member.getRole() == MemberRole.OWNER) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Owner cannot remove themselves",
                            "Delete the pending session instead"
                    )
            );
        }

        if (session.get().getStatus() != CollabQuestStatus.PENDING) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "Only pending sessions can remove invited members",
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

    private void deleteMemberProgress(Long userId, Long questId) {
        var questUser = questUserRepository.findFirstByUserIdAndQuestIdAndStatusIn(
                userId,
                questId,
                ACTIVE_QUEST_USER_STATUSES
        );
        if (questUser.isEmpty()) {
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
}
