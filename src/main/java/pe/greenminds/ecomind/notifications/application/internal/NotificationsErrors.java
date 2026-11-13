package pe.greenminds.ecomind.notifications.application.internal;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;

/** Errors of the Notifications context; their messages are translated in messages.properties. */
public final class NotificationsErrors {

  private NotificationsErrors() {
  }

  public static ApplicationError notificationNotFound(Long notificationId) {
    return ApplicationError.notFound("NOTIFICATION", String.valueOf(notificationId));
  }

  public static ApplicationError notificationAccessForbidden() {
    return ApplicationError.forbidden(
        "NOTIFICATION_ACCESS_FORBIDDEN", "Only the recipient can manage a notification");
  }

  public static ApplicationError alreadyNotified(String sourceKey) {
    return ApplicationError.conflict(
        "notification", "The source " + sourceKey + " already notified this user");
  }

  public static ApplicationError invalidNotification(String reason) {
    return ApplicationError.validationError("notification", reason);
  }
}
