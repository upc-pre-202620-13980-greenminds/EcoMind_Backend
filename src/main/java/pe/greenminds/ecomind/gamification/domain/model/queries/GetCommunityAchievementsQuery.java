package pe.greenminds.ecomind.gamification.domain.model.queries;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.util.UUID;

public record GetCommunityAchievementsQuery(
        UUID communityId, UserId requestedBy, int page, int size) {}
