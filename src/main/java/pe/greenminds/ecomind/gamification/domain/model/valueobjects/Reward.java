package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

/** XP is the ecopoints score. Monetization owns the separate gem balance. */
public record Reward(long ecopoints, int gems) {

    public Reward {
        if (ecopoints < 0 || gems < 0) {
            throw new IllegalArgumentException("Reward amounts cannot be negative");
        }
    }
}
