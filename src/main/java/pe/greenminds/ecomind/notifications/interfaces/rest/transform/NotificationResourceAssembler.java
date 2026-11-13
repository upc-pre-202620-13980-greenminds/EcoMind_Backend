package pe.greenminds.ecomind.notifications.interfaces.rest.transform;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.interfaces.rest.resources.NotificationResource;

/**
 * Converts a notification into a resource. A component and not a static assembler because the
 * texts of the generated notifications come from messages.properties, in the language of the
 * request (Accept-Language), like the error messages.
 */
@Component
public class NotificationResourceAssembler {

  private final MessageSource messageSource;

  public NotificationResourceAssembler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public NotificationResource toResourceFromEntity(Notification notification) {
    String title = notification.getTitle();
    String message = notification.getMessage();
    if (notification.getType() != NotificationType.GENERAL) {
      Locale locale = LocaleContextHolder.getLocale();
      String key = "notification." + notification.getType().name().toLowerCase(Locale.ROOT)
          .replace('_', '-');
      Object[] args = {notification.getActorName(), notification.getDetail()};
      title = messageSource.getMessage(key + ".title", args, locale);
      message = messageSource.getMessage(key + ".message", args, locale);
    }
    return new NotificationResource(
        notification.getId(),
        notification.getCategory().name(),
        notification.getType().name(),
        title,
        message,
        notification.isRead(),
        notification.getCreatedAt());
  }
}
