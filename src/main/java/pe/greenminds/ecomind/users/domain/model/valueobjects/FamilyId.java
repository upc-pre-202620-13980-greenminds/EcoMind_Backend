package pe.greenminds.ecomind.users.domain.model.valueobjects;

public record FamilyId(Long value) {

  public FamilyId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("Family id must be a positive number");
    }
  }
}
