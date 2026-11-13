package pe.greenminds.ecomind.notifications.application.commandservices;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.commands.CreateNotificationCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkAllNotificationsAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyUserCommand;

/**
 * Entry point for the other bounded contexts too: they create their notifications through this
 * service, not through the REST endpoint.
 */
public interface NotificationCommandService {

  Result<Notification, ApplicationError> handle(CreateNotificationCommand command);

  Result<Notification, ApplicationError> handle(NotifyFriendRequestAcceptedCommand command);

  /**
   * Creates a notification produced by another bounded context or a scheduled task. It runs in its
   * own transaction, so a failure never undoes the work of the context that published the event.
   * When the source already notified the recipient it returns NOTIFICATION_CONFLICT.
   */
  Result<Notification, ApplicationError> handle(NotifyUserCommand command);

  Result<Notification, ApplicationError> handle(MarkNotificationAsReadCommand command);

  /** Returns how many notifications changed from unread to read. */
  Result<Integer, ApplicationError> handle(MarkAllNotificationsAsReadCommand command);
}
