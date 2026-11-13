package pe.greenminds.ecomind.notifications.domain.model.commands;

import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

public record MarkAllNotificationsAsReadCommand(RecipientId requestedBy) {
}
