package pe.greenminds.ecomind.users.domain.model.valueobjects;

/**
 * Identifier of a user profile. It has the same value as the account id issued by IAM.
 */
public record UserId(Long value) {

  public UserId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("User id must be a positive number");
    }
  }
}
