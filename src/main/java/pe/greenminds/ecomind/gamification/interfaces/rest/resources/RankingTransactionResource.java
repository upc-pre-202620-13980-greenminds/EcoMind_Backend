package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record RankingTransactionResource(
        UUID id, Long beneficiaryId, long ecopoints, Instant occurredAt) {}
