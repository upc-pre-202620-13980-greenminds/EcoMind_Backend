package pe.greenminds.ecomind.community.domain.model.events;

public record CommunityGoalCompletedEvent(Long communityGoalId, Long communityId, String title) {}
