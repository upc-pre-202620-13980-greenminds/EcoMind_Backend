package pe.greenminds.ecomind.notifications.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.notifications.application.queryservices.NotificationQueryService;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.queries.GetNotificationsByRecipientQuery;
import pe.greenminds.ecomind.notifications.domain.model.queries.GetUnreadCountQuery;
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository;

@Service
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

  private final NotificationRepository notificationRepository;

  public NotificationQueryServiceImpl(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override
  public List<Notification> handle(GetNotificationsByRecipientQuery query) {
    return notificationRepository.findByRecipient(query.recipientId());
  }

  @Override
  public long handle(GetUnreadCountQuery query) {
    return notificationRepository.countUnreadByRecipient(query.recipientId());
  }
}
