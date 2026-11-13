package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.Instant;

public record CommunityGoalEnrollment(
        Long userId,
        Long goalId,
        Instant joinedAt) {
}