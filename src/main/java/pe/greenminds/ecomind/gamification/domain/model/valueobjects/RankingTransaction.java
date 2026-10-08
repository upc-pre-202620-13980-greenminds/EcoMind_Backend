package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.time.Instant;
import java.util.UUID;

/** Minimal data needed for client-side period aggregation; no XP, gems or private profile data. */
public record RankingTransaction(UUID id, Long beneficiaryId, long ecopoints, Instant occurredAt) {}
