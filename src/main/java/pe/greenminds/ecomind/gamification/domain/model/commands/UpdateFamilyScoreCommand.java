package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

import java.time.Instant;
import java.util.UUID;

public record UpdateFamilyScoreCommand(
        FamilyId familyId, long ecopoints, UUID executionId, Instant occurredAt) {}
