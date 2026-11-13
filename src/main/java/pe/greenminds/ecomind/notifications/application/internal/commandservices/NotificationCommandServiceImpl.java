package pe.greenminds.ecomind.notifications.application.internal.commandservices;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.application.internal.NotificationsErrors;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.commands.CreateNotificationCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkAllNotificationsAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyUserCommand;
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository;

@Service
public class NotificationCommandServiceImpl implements NotificationCommandService {

  private final NotificationRepository notificationRepository;

  public NotificationCommandServiceImpl(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override
  @Transactional
  public Result<Notification, ApplicationError> handle(CreateNotificationCommand command) {
    try {
      Notification notification =
          Notification.general(
              command.recipientId(), command.category(), command.title(), command.message());
      return Result.success(notificationRepository.save(notification));
    } catch (IllegalArgumentException exception) {
      return Result.failure(NotificationsErrors.invalidNotification(exception.getMessage()));
    }
  }

  @Override
  // Called after the friendship is saved, so it needs its own transaction
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Result<Notification, ApplicationError> handle(NotifyFriendRequestAcceptedCommand command) {
    try {
      Notification notification =
          Notification.friendRequestAccepted(command.requesterId(), command.accepterName());
      return Result.success(notificationRepository.save(notification));
    } catch (IllegalArgumentException exception) {
      return Result.failure(NotificationsErrors.invalidNotification(exception.getMessage()));
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Result<Notification, ApplicationError> handle(NotifyUserCommand command) {
    // An event delivered twice or a task run again finds the notification it already created
    if (notificationRepository.existsByRecipientAndSourceKey(
        command.recipientId(), command.sourceKey())) {
      return Result.failure(NotificationsErrors.alreadyNotified(command.sourceKey()));
    }
    try {
      Notification notification =
          Notification.generated(
              command.recipientId(),
              command.category(),
              command.type(),
              command.subject(),
              command.detail(),
              command.sourceKey());
      return Result.success(notificationRepository.save(notification));
    } catch (IllegalArgumentException exception) {
      return Result.failure(NotificationsErrors.invalidNotification(exception.getMessage()));
    }
  }

  @Override
  @Transactional
  public Result<Notification, ApplicationError> handle(MarkNotificationAsReadCommand command) {
    var found = notificationRepository.findById(command.notificationId());
    if (found.isEmpty()) {
      return Result.failure(NotificationsErrors.notificationNotFound(command.notificationId()));
    }
    Notification notification = found.get();
    if (!notification.belongsTo(command.requestedBy())) {
      return Result.failure(NotificationsErrors.notificationAccessForbidden());
    }
    // Marking an already read notification again changes nothing, so it is not an error
    notification.markAsRead();
    return Result.success(notificationRepository.save(notification));
  }

  @Override
  @Transactional
  public Result<Integer, ApplicationError> handle(MarkAllNotificationsAsReadCommand command) {
    List<Notification> unread = notificationRepository.findUnreadByRecipient(command.requestedBy());
    unread.forEach(notification -> {
      notification.markAsRead();
      notificationRepository.save(notification);
    });
    return Result.success(unread.size());
  }
}
