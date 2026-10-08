package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.util.UUID;

public record AchievementResource(
        UUID id,
        String code,
        String name,
        String description,
        String scope,
        String metric,
        long target,
        boolean active) {}
