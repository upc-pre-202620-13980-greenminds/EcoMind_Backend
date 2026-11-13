package pe.greenminds.ecomind.community.interfaces.rest.resources;

public record CommunityGoalPanelResource(
        CommunityGoalResource goal,
        boolean can_create,
        boolean joined,
        boolean other_goal_active,
        int member_count,
        int target_preview) {
}