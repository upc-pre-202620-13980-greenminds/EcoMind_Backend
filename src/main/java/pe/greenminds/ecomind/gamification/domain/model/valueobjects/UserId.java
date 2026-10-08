package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

/** External Users identity, represented locally without depending on its domain model. */
public record UserId(Long value) {
    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }
}
