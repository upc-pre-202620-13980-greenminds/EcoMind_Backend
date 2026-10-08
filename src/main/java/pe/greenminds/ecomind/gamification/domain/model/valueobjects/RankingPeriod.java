package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.time.Instant;

/** UTC interval: inclusive start, exclusive end, so adjacent periods never count a grant twice. */
public record RankingPeriod(Instant from, Instant to) {
    public RankingPeriod {
        if (from == null || to == null || !from.isBefore(to))
            throw new IllegalArgumentException("A valid from/to period is required");
    }
}
