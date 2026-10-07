package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestCounters;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestPermissions;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestSource;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestSessionQueryService;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestSessionState;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestSessionStateQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
public class CollabQuestSessionQueryServiceImpl implements CollabQuestSessionQueryService {
    private static final int MAX_INVITES = 5;
    private static final List<CollabQuestStatus> ACTIVE_SESSION_STATUSES =
            List.of(CollabQuestStatus.PENDING, CollabQuestStatus.STARTED);
    private static final List<QuestStatus> ACTIVE_QUEST_USER_STATUSES =
            List.of(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE);

    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final QuestUserRepository questUserRepository;
    private final ActivityRepository activityRepository;
    private final QuestRepository questRepository;
    private final FamilyPlanItemRepository familyPlanItemRepository;

    public CollabQuestSessionQueryServiceImpl(
            CollabQuestSessionRepository collabQuestSessionRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            QuestUserRepository questUserRepository,
            ActivityRepository activityRepository,
            QuestRepository questRepository,
            FamilyPlanItemRepository familyPlanItemRepository
    ) {
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.questUserRepository = questUserRepository;
        this.activityRepository = activityRepository;
        this.questRepository = questRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
    }

    @Override
    public CollabQuestSessionState handle(GetCollabQuestSessionStateQuery query) {
        var questUser = questUserRepository
                .findFirstByUserIdAndQuestIdAndStatusIn(
                        query.userId(),
                        query.questId(),
                        ACTIVE_QUEST_USER_STATUSES
                )
                .orElse(null);
        var session = questUser != null && questUser.getCollaborativeSessionId() != null
                ? collabQuestSessionRepository
                        .findById(questUser.getCollaborativeSessionId())
                        .orElse(null)
                : findSessionByOwnerOrMember(query);
        var members = session == null
                ? List.<CollabQuestMember>of()
                : collabQuestMemberRepository.findBySessionIdAndStatusIn(
                        session.getId(),
                        List.of(CollabMemberStatus.PENDING, CollabMemberStatus.ACCEPTED)
                );

        var currentMember = findCurrentMember(members, query.userId());
        var pendingInvitation = currentMember != null
                && currentMember.getStatus() == CollabMemberStatus.PENDING
                ? currentMember
                : null;
        var counters = buildCounters(members);
        var permissions = buildPermissions(
                query,
                session,
                members,
                currentMember,
                pendingInvitation,
                counters
        );
        var unavailableUserIds = findUnavailableUserIds(query.questId());
        var familyPlanItem = session == null
                ? java.util.Optional.<pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem>empty()
                : familyPlanItemRepository.findByCollaborativeSessionId(session.getId());

        return new CollabQuestSessionState(
                session,
                members,
                currentMember,
                pendingInvitation,
                permissions,
                counters,
                unavailableUserIds,
                familyPlanItem.isPresent()
                        ? CollabQuestSource.FAMILY_PLAN
                        : CollabQuestSource.COLLABORATIVE,
                familyPlanItem.map(pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem::getFamilyPlanId)
                        .orElse(null),
                familyPlanItem.map(pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem::getId)
                        .orElse(null)
        );
    }

    private pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession
    findSessionByOwnerOrMember(GetCollabQuestSessionStateQuery query) {
        var ownerSession = collabQuestSessionRepository
                .findByQuestIdAndOwnerUserIdAndStatusIn(
                        query.questId(),
                        query.userId(),
                        ACTIVE_SESSION_STATUSES
                );
        if (ownerSession.isPresent()) {
            return ownerSession.get();
        }

        return collabQuestMemberRepository
                .findByUserIdAndQuestIdAndSessionStatusIn(
                        query.userId(),
                        query.questId(),
                        ACTIVE_SESSION_STATUSES
                )
                .stream()
                .filter(member -> member.getStatus() == CollabMemberStatus.PENDING
                        || member.getStatus() == CollabMemberStatus.ACCEPTED)
                .findFirst()
                .flatMap(member -> collabQuestSessionRepository.findById(member.getSessionId()))
                .orElse(null);
    }

    private CollabQuestMember findCurrentMember(List<CollabQuestMember> members, Long userId) {
        return members.stream()
                .filter(member -> Objects.equals(member.getUserId(), userId))
                .findFirst()
                .orElse(null);
    }

    private CollabQuestCounters buildCounters(List<CollabQuestMember> members) {
        var acceptedInvites = (int) members.stream()
                .filter(member -> member.getStatus() == CollabMemberStatus.ACCEPTED)
                .count();
        var pendingInvites = (int) members.stream()
                .filter(member -> member.getStatus() == CollabMemberStatus.PENDING)
                .count();

        return new CollabQuestCounters(
                acceptedInvites,
                pendingInvites,
                acceptedInvites + pendingInvites,
                MAX_INVITES
        );
    }

    private CollabQuestPermissions buildPermissions(
            GetCollabQuestSessionStateQuery query,
            CollabQuestSession session,
            List<CollabQuestMember> members,
            CollabQuestMember currentMember,
            CollabQuestMember pendingInvitation,
            CollabQuestCounters counters
    ) {
        if (session == null) {
            var userHasActiveQuestUser = questUserRepository
                    .findFirstByUserIdAndQuestIdAndStatusIn(
                            query.userId(),
                            query.questId(),
                            ACTIVE_QUEST_USER_STATUSES
                    )
                    .isPresent();
            var quest = questRepository.findById(query.questId());
            var questIsCollaborative = quest.isPresent()
                    && quest.get().getType() == QuestType.COLLABORATIVE;
            var questHasActivities = activityRepository.countByQuestId(query.questId()) > 0;
            return new CollabQuestPermissions(
                    !userHasActiveQuestUser && questIsCollaborative && questHasActivities,
                    !userHasActiveQuestUser && questHasActivities,
                    false,
                    false,
                    false,
                    false
            );
        }

        var isOwner = Objects.equals(session.getOwnerId(), query.userId());
        var isPendingSession = session.getStatus() == CollabQuestStatus.PENDING;
        var isFamilyPlanSession = familyPlanItemRepository.existsByCollaborativeSessionId(
                session.getId()
        );
        var userHasQuestUser = questUserRepository
                .findFirstByUserIdAndQuestIdAndStatusIn(
                        query.userId(),
                        query.questId(),
                        ACTIVE_QUEST_USER_STATUSES
                )
                .isPresent();

        var canInvite = isOwner
                && isPendingSession
                && !isFamilyPlanSession
                && !userHasQuestUser
                && counters.activeInvites() < counters.maxInvites();

        var canStart = isOwner
                && isPendingSession
                && counters.acceptedInvites() >= 2
                && activityRepository.countByQuestId(query.questId()) > 0
                && acceptedMembersAreAvailable(members, query.questId());

        var canAcceptInvitation = pendingInvitation != null
                && isPendingSession
                && !userHasQuestUser;

        var canLeave = currentMember != null
                && currentMember.getRole() != MemberRole.OWNER
                && currentMember.getStatus() == CollabMemberStatus.ACCEPTED
                && (isPendingSession || session.getStatus() == CollabQuestStatus.STARTED);

        var canRemoveMembers = isOwner && isPendingSession && !isFamilyPlanSession;
        var canDeleteSession = isOwner && isPendingSession && !isFamilyPlanSession;

        return new CollabQuestPermissions(
                canInvite,
                canStart,
                canAcceptInvitation,
                isFamilyPlanSession ? false : canLeave,
                canRemoveMembers,
                canDeleteSession
        );
    }

    private boolean acceptedMembersAreAvailable(
            List<CollabQuestMember> members,
            Long questId
    ) {
        return members.stream()
                .filter(member -> member.getStatus() == CollabMemberStatus.ACCEPTED)
                .noneMatch(member -> questUserRepository
                        .findFirstByUserIdAndQuestIdAndStatusIn(
                                member.getUserId(),
                                questId,
                                ACTIVE_QUEST_USER_STATUSES
                        )
                        .isPresent());
    }

    private List<Long> findUnavailableUserIds(Long questId) {
        var unavailableUserIds = new LinkedHashSet<Long>();

        questUserRepository.findByQuestIdAndStatusIn(questId, ACTIVE_QUEST_USER_STATUSES)
                .stream()
                .map(questUser -> questUser.getUserId())
                .forEach(unavailableUserIds::add);

        collabQuestSessionRepository.findByQuestId(questId)
                .stream()
                .filter(session -> ACTIVE_SESSION_STATUSES.contains(session.getStatus()))
                .flatMap(session -> collabQuestMemberRepository
                        .findBySessionIdAndStatusIn(
                                session.getId(),
                                List.of(CollabMemberStatus.ACCEPTED)
                        )
                        .stream())
                .map(CollabQuestMember::getUserId)
                .forEach(unavailableUserIds::add);

        return List.copyOf(unavailableUserIds);
    }
}
