package pe.greenminds.ecomind.gamification.domain.services;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.ActiveMultiplier;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

public class RewardCalculationService {
    public Reward calculate(
            Reward base,
            Optional<ActiveMultiplier> multiplier,
            Instant at,
            BigDecimal repetitionFactor) {
        if (repetitionFactor.signum() < 0 || repetitionFactor.compareTo(BigDecimal.ONE) > 0)
            throw new IllegalArgumentException("Invalid repetition factor");
        var xpFactor =
                multiplier
                        .filter(m -> m.isActiveAt(at))
                        .map(ActiveMultiplier::factor)
                        .orElse(BigDecimal.ONE);
        return new Reward(
                scale(base.ecopoints(), repetitionFactor.multiply(xpFactor)),
                Math.toIntExact(scale(base.gems(), repetitionFactor)));
    }

    private long scale(long value, BigDecimal factor) {
        return BigDecimal.valueOf(value)
                .multiply(factor)
                .setScale(0, RoundingMode.FLOOR)
                .longValueExact();
    }
}
