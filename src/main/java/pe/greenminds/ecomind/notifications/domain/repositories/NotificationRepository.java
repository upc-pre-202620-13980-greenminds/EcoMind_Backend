package pe.greenminds.ecomind.notifications.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

public interface NotificationRepository {

  Notification save(Notification notification);

  Optional<Notification> findById(Long notificationId);

  /** Notifications of the user, the newest first. */
  List<Notification> findByRecipient(RecipientId recipientId);

  List<Notification> findUnreadByRecipient(RecipientId recipientId);

  long countUnreadByRecipient(RecipientId recipientId);

  /** True when the source already notified this recipient. */
  boolean existsByRecipientAndSourceKey(RecipientId recipientId, String sourceKey);
}
