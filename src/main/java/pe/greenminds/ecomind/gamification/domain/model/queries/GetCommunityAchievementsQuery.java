package pe.greenminds.ecomind.gamification.domain.model.queries;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public record GetCommunityAchievementsQuery(
        Long communityId, UserId requestedBy, int page, int size) {}
