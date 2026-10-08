package pe.greenminds.ecomind.gamification.domain.model.commands;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.UUID;

public record UpdateUserScoreCommand(
        UserId userId, Reward reward, UUID executionId, Instant occurredAt) {}
