package pe.greenminds.ecomind.notifications.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.queries.GetNotificationsByRecipientQuery;
import pe.greenminds.ecomind.notifications.domain.model.queries.GetUnreadCountQuery;

public interface NotificationQueryService {

  List<Notification> handle(GetNotificationsByRecipientQuery query);

  long handle(GetUnreadCountQuery query);
}
