package pe.greenminds.ecomind.notifications.application.commandservices;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.commands.CreateNotificationCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkAllNotificationsAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;

/**
 * Entry point for the other bounded contexts too: they create their notifications through this
 * service, not through the REST endpoint.
 */
public interface NotificationCommandService {

  Result<Notification, ApplicationError> handle(CreateNotificationCommand command);

  Result<Notification, ApplicationError> handle(NotifyFriendRequestAcceptedCommand command);

  Result<Notification, ApplicationError> handle(MarkNotificationAsReadCommand command);

  /** Returns how many notifications changed from unread to read. */
  Result<Integer, ApplicationError> handle(MarkAllNotificationsAsReadCommand command);
}
