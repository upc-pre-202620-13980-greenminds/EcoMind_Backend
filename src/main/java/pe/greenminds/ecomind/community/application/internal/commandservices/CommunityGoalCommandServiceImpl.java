package pe.greenminds.ecomind.community.application.internal.commandservices;

import java.util.Comparator;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pe.greenminds.ecomind.community.application.commandservices.CommunityGoalCommandService;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoalEnrollment;
import pe.greenminds.ecomind.community.domain.model.aggregates.Post;
import pe.greenminds.ecomind.community.domain.model.commands.CreateCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.commands.JoinCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.commands.RecordGoalQuestCompletionCommand;
import pe.greenminds.ecomind.community.domain.model.events.CommunityGoalCompletedEvent;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.domain.repositories.*;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class CommunityGoalCommandServiceImpl implements CommunityGoalCommandService {

    private final CommunityGoalRepository goals;
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final CommunityAchievementRepository achievements;
    private final ApplicationEventPublisher publisher;
    private final PostRepository posts;
    private final CommunityGoalParticipationRepository participation;

    public CommunityGoalCommandServiceImpl(
            CommunityGoalRepository goals,
            CommunityRepository communities,
            CommunityMembershipRepository memberships,
            CommunityAchievementRepository achievements,
            ApplicationEventPublisher publisher,
            PostRepository posts,
            CommunityGoalParticipationRepository participation) {
        this.goals = goals;
        this.communities = communities;
        this.memberships = memberships;
        this.achievements = achievements;
        this.publisher = publisher;
        this.posts = posts;
        this.participation = participation;
    }

    @Override
    @Transactional
    public Result<CommunityGoal, ApplicationError> handle(CreateCommunityGoalCommand command) {
        var community = communities.findByIdForUpdate(command.communityId());

        if (community.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Community", String.valueOf(command.communityId())));
        }

        if (command.topic() == null) {
            return Result.failure(ApplicationError.validationError(
                    "Community goal", "A supported topic is required"));
        }

        boolean thematic = community.get().getType() == CommunityType.TOPIC;

        if (thematic) {
            boolean admin = memberships.findByCommunityIdAndUserId(
                            command.communityId(), command.requestedBy())
                    .map(member -> member.role() == CommunityRole.ADMIN)
                    .orElse(false);

            if (!admin) {
                return Result.failure(ApplicationError.forbidden(
                        "COMMUNITY_GOAL_ADMIN_REQUIRED",
                        "Only the community administrator can create this goal"));
            }

            if (participation.findByUserId(command.requestedBy()).isPresent()) {
                return alreadyParticipating();
            }
        }

        if (goals.existsActiveByCommunityId(command.communityId())) {
            return Result.failure(ApplicationError.conflict(
                    "Community goal", "The community already has an active goal"));
        }

        long memberCount = memberships.countByCommunityId(command.communityId());

        if (memberCount < 1 || memberCount > Integer.MAX_VALUE / 10L) {
            return Result.failure(ApplicationError.validationError(
                    "Community goal",
                    "The community must have a supported number of members"));
        }

        int target = Math.toIntExact(memberCount * 10L);

        CommunityGoal saved = goals.save(new CommunityGoal(
                null,
                command.communityId(),
                command.topic(),
                target,
                0,
                thematic ? 1 : 0,
                CommunityGoalStatus.ACTIVE));

        if (thematic && !participation.enroll(command.requestedBy(), saved.id())) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return alreadyParticipating();
        }

        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<CommunityGoal, ApplicationError> handle(JoinCommunityGoalCommand command) {
        var initial = goals.findById(command.goalId());

        if (initial.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Community goal", String.valueOf(command.goalId())));
        }

        if (communities.findByIdForUpdate(initial.get().communityId()).isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Community", String.valueOf(initial.get().communityId())));
        }

        var found = goals.findByIdForUpdate(command.goalId());

        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Community goal", String.valueOf(command.goalId())));
        }

        CommunityGoal goal = found.get();

        if (goal.status() != CommunityGoalStatus.ACTIVE) {
            return Result.failure(ApplicationError.conflict(
                    "Community goal", "This goal is already completed"));
        }

        if (memberships.findByCommunityIdAndUserId(
                goal.communityId(), command.requestedBy()).isEmpty()) {
            return Result.failure(ApplicationError.forbidden(
                    "COMMUNITY_MEMBERSHIP_REQUIRED",
                    "Join the community before joining its goal"));
        }

        var existing = participation.findByUserId(command.requestedBy());

        if (existing.isPresent()) {
            if (existing.get().goalId().equals(goal.id())) {
                return Result.success(goal);
            }
            return alreadyParticipating();
        }

        if (!participation.enroll(command.requestedBy(), goal.id())) {
            return alreadyParticipating();
        }

        return Result.success(goals.save(withProgress(
                goal,
                goal.progress(),
                Math.toIntExact(participation.countByGoalId(goal.id())))));
    }

    @Override
    @Transactional
    public void handle(RecordGoalQuestCompletionCommand command) {
        var enrollments = command.userIds().stream()
                .distinct()
                .map(userId -> participation.findByUserId(userId).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CommunityGoalEnrollment::goalId)
                        .thenComparing(CommunityGoalEnrollment::userId))
                .toList();

        var enrolledUsers = enrollments.stream()
                .map(CommunityGoalEnrollment::userId)
                .collect(java.util.stream.Collectors.toSet());

        for (var enrollment : enrollments) {
            recordContribution(command, enrollment);
        }

        command.userIds().stream()
                .distinct()
                .filter(userId -> !enrolledUsers.contains(userId))
                .sorted()
                .forEach(userId -> participation.registerCompletion(
                        command.sourceKey() + ":" + userId));
    }

    private void recordContribution(
            RecordGoalQuestCompletionCommand command,
            CommunityGoalEnrollment expected) {
        var found = goals.findByIdForUpdate(expected.goalId());

        String receiptKey = command.sourceKey() + ":" + expected.userId();

        if (!participation.registerCompletion(receiptKey)) {
            return;
        }

        if (found.isEmpty() || found.get().status() != CommunityGoalStatus.ACTIVE) {
            return;
        }

        var currentEnrollment = participation.findByUserId(expected.userId());

        if (currentEnrollment.isEmpty()
                || !currentEnrollment.get().goalId().equals(expected.goalId())
                || command.completedAt().isBefore(currentEnrollment.get().joinedAt())) {
            return;
        }

        CommunityGoal current = found.get();

        if (current.topic() != command.topic()) {
            return;
        }

        if (memberships.findByCommunityIdAndUserId(
                current.communityId(), expected.userId()).isEmpty()) {
            return;
        }

        int progress = Math.min(current.target(), current.progress() + 1);
        int participants = Math.toIntExact(participation.countByGoalId(current.id()));

        CommunityGoal updated = goals.save(withProgress(
                current, progress, participants));

        if (updated.status() == CommunityGoalStatus.COMPLETED) {
            achievements.save(new CommunityAchievement(
                    null,
                    updated.communityId(),
                    "Community goal completed",
                    updated.title(),
                    updated.id()));

            posts.save(new Post(
                    null,
                    updated.communityId(),
                    null,
                    "We completed our community goal: " + updated.title()
                            + ". Achievement earned: Community goal completed.",
                    "COMMUNITY_GOAL_COMPLETED",
                    null,
                    null));

            participation.releaseGoal(updated.id());

            publisher.publishEvent(new CommunityGoalCompletedEvent(
                    updated.id(),
                    updated.communityId(),
                    updated.title()));
        }
    }

    @Override
    @Transactional
    public void leaveCommunity(Long communityId, Long userId) {
        var enrollment = participation.findByUserId(userId);

        if (enrollment.isEmpty()) {
            return;
        }

        var initial = goals.findById(enrollment.get().goalId());

        if (initial.isEmpty() || !initial.get().communityId().equals(communityId)) {
            return;
        }

        var locked = goals.findByIdForUpdate(initial.get().id());

        if (locked.isEmpty()) {
            return;
        }

        var currentEnrollment = participation.findByUserId(userId);

        if (currentEnrollment.isEmpty()
                || !currentEnrollment.get().goalId().equals(locked.get().id())) {
            return;
        }

        participation.remove(userId, locked.get().id());

        if (locked.get().status() == CommunityGoalStatus.ACTIVE) {
            goals.save(withProgress(
                    locked.get(),
                    locked.get().progress(),
                    Math.toIntExact(participation.countByGoalId(locked.get().id()))));
        }
    }

    private CommunityGoal withProgress(
            CommunityGoal goal,
            int progress,
            int participants) {
        return new CommunityGoal(
                goal.id(),
                goal.communityId(),
                goal.topic(),
                goal.target(),
                progress,
                participants,
                progress >= goal.target()
                        ? CommunityGoalStatus.COMPLETED
                        : CommunityGoalStatus.ACTIVE);
    }

    private Result<CommunityGoal, ApplicationError> alreadyParticipating() {
        return Result.failure(ApplicationError.conflict(
                "Community goal participation",
                "You already participate in an active community goal"));
    }
}