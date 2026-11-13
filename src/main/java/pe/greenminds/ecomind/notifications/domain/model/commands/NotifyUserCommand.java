package pe.greenminds.ecomind.notifications.domain.model.commands;

import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

/**
 * Sent by the consumers of the other bounded contexts and by the scheduled tasks. The source key
 * identifies what produced it, such as "ACHIEVEMENT:{awardId}", so it is created only once.
 */
public record NotifyUserCommand(
    RecipientId recipientId,
    NotificationCategory category,
    NotificationType type,
    String subject,
    String detail,
    String sourceKey) {
}
