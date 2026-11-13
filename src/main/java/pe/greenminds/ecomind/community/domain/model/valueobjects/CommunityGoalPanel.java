package pe.greenminds.ecomind.community.domain.model.valueobjects;

import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;

public record CommunityGoalPanel(
        CommunityGoal goal,
        boolean canCreate,
        boolean joined,
        boolean otherGoalActive,
        int memberCount,
        int targetPreview) {
}