package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.util.Objects;

public record RewardBeneficiary(BeneficiaryType type, Long id) {
    public RewardBeneficiary {
        Objects.requireNonNull(type);
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid beneficiary id");
    }
}
