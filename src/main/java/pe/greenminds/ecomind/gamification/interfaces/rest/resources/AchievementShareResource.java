package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record AchievementShareResource(
        UUID requestId,
        UUID awardId,
        Long requestedBy,
        Long communityId,
        String status,
        Long publicationId,
        Instant createdAt,
        Instant confirmedAt) {}
