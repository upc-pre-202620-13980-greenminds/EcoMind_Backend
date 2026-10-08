package pe.greenminds.ecomind.gamification.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/** Internal evaluation request. Values are loaded from confirmed Gamification progress. */
public record AwardAchievementCommand(
        UUID achievementId,
        Long beneficiaryId,
        UUID communityId,
        UUID sourceEventId,
        Instant occurredAt) {}
