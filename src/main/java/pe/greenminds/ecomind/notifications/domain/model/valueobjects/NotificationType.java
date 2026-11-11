package pe.greenminds.ecomind.notifications.domain.model.valueobjects;

/**
 * What produced the notification. A generated type keeps only its data and its texts are written
 * in the language of whoever reads it; a GENERAL one keeps the texts it was created with.
 */
public enum NotificationType {
  GENERAL,
  FRIEND_REQUEST_ACCEPTED
}
