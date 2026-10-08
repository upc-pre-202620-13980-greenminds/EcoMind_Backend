package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ShareAchievementResource(
        @NotNull UUID requestId, @NotNull UUID awardId, @NotNull @Positive Long communityId) {}
