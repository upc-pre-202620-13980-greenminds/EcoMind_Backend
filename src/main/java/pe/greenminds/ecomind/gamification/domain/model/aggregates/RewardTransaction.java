package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.BeneficiaryType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardBeneficiary;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSource;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardSourceType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Immutable grant identified by canonical execution and beneficiary. */
public record RewardTransaction(
        UUID id,
        RewardSourceType sourceType,
        UUID sourceExecutionId,
        UserId beneficiary,
        Reward baseReward,
        Reward grantedReward,
        Instant occurredAt,
        UUID multiplierId,
        BigDecimal appliedFactor,
        BigDecimal repetitionFactor) {
    public RewardTransaction(
            UUID id,
            RewardSourceType sourceType,
            UUID sourceExecutionId,
            UserId beneficiary,
            Reward baseReward,
            Reward grantedReward,
            Instant occurredAt) {
        this(
                id,
                sourceType,
                sourceExecutionId,
                beneficiary,
                baseReward,
                grantedReward,
                occurredAt,
                null,
                BigDecimal.ONE,
                BigDecimal.ONE);
    }

    public RewardSource source() {
        return new RewardSource(sourceType, sourceExecutionId);
    }

    public RewardBeneficiary recipient() {
        return new RewardBeneficiary(BeneficiaryType.USER, beneficiary.value());
    }

    public RewardTransaction {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sourceType);
        Objects.requireNonNull(sourceExecutionId);
        Objects.requireNonNull(beneficiary);
        Objects.requireNonNull(baseReward);
        Objects.requireNonNull(grantedReward);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(appliedFactor);
        Objects.requireNonNull(repetitionFactor);
        appliedFactor = appliedFactor.setScale(8, RoundingMode.UNNECESSARY).stripTrailingZeros();
        repetitionFactor =
                repetitionFactor.setScale(8, RoundingMode.UNNECESSARY).stripTrailingZeros();
        if (appliedFactor.signum() < 0
                || repetitionFactor.signum() < 0
                || repetitionFactor.compareTo(BigDecimal.ONE) > 0)
            throw new IllegalArgumentException("Invalid applied reward factor");
    }
}
