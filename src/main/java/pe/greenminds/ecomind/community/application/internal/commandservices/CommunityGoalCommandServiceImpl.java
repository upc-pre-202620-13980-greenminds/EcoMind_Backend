package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.CommunityGoalCommandService;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.model.commands.CreateCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.commands.IncrementCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.events.CommunityGoalCompletedEvent;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;
import pe.greenminds.ecomind.community.domain.repositories.CommunityAchievementRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityGoalRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class CommunityGoalCommandServiceImpl implements CommunityGoalCommandService {
    private final CommunityGoalRepository goals;
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final CommunityAchievementRepository achievements;
    private final ApplicationEventPublisher publisher;

    public CommunityGoalCommandServiceImpl(CommunityGoalRepository goals, CommunityRepository communities,
            CommunityMembershipRepository memberships, CommunityAchievementRepository achievements,
            ApplicationEventPublisher publisher) {
        this.goals = goals;
        this.communities = communities;
        this.memberships = memberships;
        this.achievements = achievements;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Result<CommunityGoal, ApplicationError> handle(CreateCommunityGoalCommand command) {
        boolean administrator = memberships.findByCommunityIdAndUserId(command.communityId(), command.requestedBy())
                .map(membership -> membership.role() == CommunityRole.ADMIN)
                .orElse(false);
        if (!administrator) {
            return Result.failure(ApplicationError.forbidden("COMMUNITY_GOAL_ADMIN_REQUIRED",
                    "Only the community administrator can manage its goals"));
        }
        if (!communities.existsById(command.communityId())) {
            return Result.failure(ApplicationError.notFound("Community", String.valueOf(command.communityId())));
        }
        if (goals.existsActiveByCommunityId(command.communityId())) {
            return Result.failure(ApplicationError.conflict("Community goal", "Community already has an active goal"));
        }
        try {
            return Result.success(goals.save(new CommunityGoal(null, command.communityId(), command.topic(),
                    command.target(), 0, 0, CommunityGoalStatus.ACTIVE)));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(ApplicationError.validationError("Community goal", exception.getMessage()));
        } catch (Exception exception) {
            return Result.failure(ApplicationError.unexpected("Community goal creation", exception.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<CommunityGoal, ApplicationError> handle(IncrementCommunityGoalCommand command) {
        var found = goals.findById(command.communityGoalId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Community goal", String.valueOf(command.communityGoalId())));
        }
        CommunityGoal current = found.get();
        if (memberships.findByCommunityIdAndUserId(current.communityId(), command.requestedBy()).isEmpty()) {
            return Result.failure(ApplicationError.forbidden("COMMUNITY_MEMBERSHIP_REQUIRED",
                    "Community membership is required"));
        }
        if (current.status() == CommunityGoalStatus.COMPLETED) {
            return Result.failure(ApplicationError.businessRuleViolation("COMMUNITY_GOAL_COMPLETED",
                    "Community goal is already completed"));
        }

        int progress = Math.min(current.target(), current.progress() + 1);
        CommunityGoalStatus status = progress >= current.target()
                ? CommunityGoalStatus.COMPLETED
                : CommunityGoalStatus.ACTIVE;
        CommunityGoal updated = goals.save(new CommunityGoal(current.id(), current.communityId(), current.topic(),
                current.target(), progress, current.participants() + 1, status));
        if (status == CommunityGoalStatus.COMPLETED) {
            achievements.save(new CommunityAchievement(null, updated.communityId(), "Community goal completed",
                    updated.title(), updated.id()));
            publisher.publishEvent(new CommunityGoalCompletedEvent(updated.id(), updated.communityId(), updated.title()));
        }
        return Result.success(updated);
    }
}
