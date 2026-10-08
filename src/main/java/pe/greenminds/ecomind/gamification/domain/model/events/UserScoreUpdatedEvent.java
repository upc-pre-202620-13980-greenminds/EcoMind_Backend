package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.UUID;

public record UserScoreUpdatedEvent(UserId userId, UUID executionId, Instant occurredAt) {}
