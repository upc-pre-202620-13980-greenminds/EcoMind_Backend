package pe.greenminds.ecomind.gamification.domain.model.commands;

import java.util.UUID;

public record ShareAchievementCommand(
        UUID requestId, UUID awardId, Long requestedBy, UUID communityId) {}
