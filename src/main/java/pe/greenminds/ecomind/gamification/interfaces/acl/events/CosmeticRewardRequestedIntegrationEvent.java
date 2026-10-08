package pe.greenminds.ecomind.gamification.interfaces.acl.events;

import java.time.Instant;
import java.util.UUID;

public record CosmeticRewardRequestedIntegrationEvent(
        UUID eventId, UUID awardId, Long userId, UUID cosmeticId, Instant occurredAt) {}
