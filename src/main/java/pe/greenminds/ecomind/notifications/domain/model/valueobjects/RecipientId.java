package pe.greenminds.ecomind.notifications.domain.model.valueobjects;

/** User who receives a notification; the same value as the account id issued by IAM. */
public record RecipientId(Long value) {

  public RecipientId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("Recipient id must be a positive number");
    }
  }
}
