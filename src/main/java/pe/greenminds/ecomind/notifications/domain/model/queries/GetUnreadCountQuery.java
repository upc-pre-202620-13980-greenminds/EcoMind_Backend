package pe.greenminds.ecomind.notifications.domain.model.queries;

import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

public record GetUnreadCountQuery(RecipientId recipientId) {
}
