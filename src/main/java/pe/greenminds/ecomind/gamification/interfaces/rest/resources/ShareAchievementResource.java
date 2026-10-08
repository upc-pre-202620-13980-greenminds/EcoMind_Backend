package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShareAchievementResource(
        @NotNull UUID requestId, @NotNull UUID awardId, @NotNull UUID communityId) {}
