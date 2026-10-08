package pe.greenminds.ecomind.iam.domain.model.valueobjects;

/**
 * Stable identifier of an account. It is the subject of the access token and the id other
 * bounded contexts use to refer to the user.
 */
public record AccountId(Long value) {

  public AccountId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("Account id must be a positive number");
    }
  }
}
