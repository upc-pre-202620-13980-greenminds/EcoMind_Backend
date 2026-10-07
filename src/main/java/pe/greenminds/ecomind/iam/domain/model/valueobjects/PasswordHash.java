package pe.greenminds.ecomind.iam.domain.model.valueobjects;

/**
 * Non-reversible representation of a password. The plain password is never part of the model.
 */
public record PasswordHash(String value) {

  public PasswordHash {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Password hash is required");
    }
  }
}
