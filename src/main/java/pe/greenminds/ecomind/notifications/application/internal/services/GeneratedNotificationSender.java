package pe.greenminds.ecomind.notifications.application.internal.services;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyUserCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

/**
 * Used by the consumers and the scheduled tasks. Each recipient gets its notification in its own
 * transaction, so one failure does not stop the others or the context that published the event.
 */
@Service
public class GeneratedNotificationSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeneratedNotificationSender.class);

  private final NotificationCommandService notificationCommandService;

  public GeneratedNotificationSender(NotificationCommandService notificationCommandService) {
    this.notificationCommandService = notificationCommandService;
  }

  public void send(
      Collection<Long> recipientIds,
      NotificationCategory category,
      NotificationType type,
      String subject,
      String detail,
      String sourceKey) {
    for (Long recipientId : recipientIds) {
      try {
        notificationCommandService.handle(
            new NotifyUserCommand(
                new RecipientId(recipientId), category, type, subject, detail, sourceKey));
      } catch (RuntimeException exception) {
        // Two deliveries at the same time hit the unique key; the first one already notified
        LOGGER.warn("Notification {} for user {} was not created", sourceKey, recipientId);
      }
    }
  }
}
