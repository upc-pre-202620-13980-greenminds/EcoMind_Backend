package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

/** Amounts granted by Gamification; Monetization owns the gem balance. */
public record Reward(long ecopoints, long experience, int gems) {

    public Reward {
        if (ecopoints < 0 || experience < 0 || gems < 0) {
            throw new IllegalArgumentException("Reward amounts cannot be negative");
        }
    }
}
