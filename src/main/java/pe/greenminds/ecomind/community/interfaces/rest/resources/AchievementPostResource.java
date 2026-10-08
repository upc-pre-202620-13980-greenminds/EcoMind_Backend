package pe.greenminds.ecomind.community.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record AchievementPostResource(
        Long id,
        UUID requestId,
        UUID awardId,
        Long authorId,
        Long communityId,
        Instant publishedAt) {}
