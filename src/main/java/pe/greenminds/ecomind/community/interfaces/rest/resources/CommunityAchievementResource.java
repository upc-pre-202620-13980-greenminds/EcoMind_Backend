package pe.greenminds.ecomind.community.interfaces.rest.resources;

public record CommunityAchievementResource(Long id, Long community_id, String title, String description,
        Long community_goal_id) {
}
