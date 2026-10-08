package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

import java.time.Instant;
import java.util.UUID;

public record FamilyScoreUpdatedEvent(FamilyId familyId, UUID executionId, Instant occurredAt) {}
