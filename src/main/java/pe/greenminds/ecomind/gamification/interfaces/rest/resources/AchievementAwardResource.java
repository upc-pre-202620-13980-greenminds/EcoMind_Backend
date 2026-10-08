package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record AchievementAwardResource(
        UUID id,
        UUID achievementId,
        String scope,
        Long beneficiaryId,
        UUID sourceEventId,
        Instant awardedAt,
        UUID communityId) {}
