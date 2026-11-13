package pe.greenminds.ecomind.community.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.queryservices.CommunityGoalPanelQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.model.valueobjects.*;
import pe.greenminds.ecomind.community.domain.repositories.*;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class CommunityGoalPanelQueryServiceImpl implements CommunityGoalPanelQueryService {

    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final CommunityGoalRepository goals;
    private final CommunityGoalParticipationRepository participation;

    public CommunityGoalPanelQueryServiceImpl(
            CommunityRepository communities,
            CommunityMembershipRepository memberships,
            CommunityGoalRepository goals,
            CommunityGoalParticipationRepository participation) {
        this.communities = communities;
        this.memberships = memberships;
        this.goals = goals;
        this.participation = participation;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<CommunityGoalPanel, ApplicationError> get(Long communityId, Long userId) {
        var community = communities.findById(communityId);

        if (community.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Community", String.valueOf(communityId)));
        }

        var membership = memberships.findByCommunityIdAndUserId(communityId, userId);

        if (membership.isEmpty()) {
            return Result.failure(ApplicationError.forbidden(
                    "COMMUNITY_MEMBERSHIP_REQUIRED", "Community membership is required"));
        }

        CommunityGoal active = goals.findAll(communityId).stream()
                .filter(goal -> goal.status() == CommunityGoalStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        var enrollment = participation.findByUserId(userId);

        boolean joined = active != null
                && enrollment.isPresent()
                && enrollment.get().goalId().equals(active.id());

        boolean otherGoalActive = enrollment.isPresent() && !joined;

        int members = Math.toIntExact(memberships.countByCommunityId(communityId));
        int targetPreview = members <= Integer.MAX_VALUE / 10 ? members * 10 : 0;

        boolean canCreate = active == null
                && community.get().getType() == CommunityType.TOPIC
                && membership.get().role() == CommunityRole.ADMIN
                && targetPreview > 0;

        if (active != null) {
            active = new CommunityGoal(
                    active.id(),
                    active.communityId(),
                    active.topic(),
                    active.target(),
                    active.progress(),
                    Math.toIntExact(participation.countByGoalId(active.id())),
                    active.status());
        }

        return Result.success(new CommunityGoalPanel(
                active,
                canCreate,
                joined,
                otherGoalActive,
                members,
                targetPreview));
    }
}