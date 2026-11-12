package pe.greenminds.ecomind.notifications.domain.model.commands;

import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

/** Creates a notification for the authenticated user, who is always the recipient. */
public record CreateNotificationCommand(
    RecipientId recipientId, NotificationCategory category, String title, String message) {
}
