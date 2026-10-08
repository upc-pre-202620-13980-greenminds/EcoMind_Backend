package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record FamilyRewardResource(
        UUID id,
        String sourceType,
        UUID sourceExecutionId,
        Long familyId,
        long ecopoints,
        Instant occurredAt) {}
