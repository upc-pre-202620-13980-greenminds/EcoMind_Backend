package pe.greenminds.ecomind.notifications.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.notifications.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;

public interface NotificationPersistenceRepository
    extends JpaRepository<NotificationPersistenceEntity, Long> {

  // The id breaks the tie between notifications created in the same instant
  List<NotificationPersistenceEntity> findByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId);

  List<NotificationPersistenceEntity> findByRecipientIdAndReadFalse(Long recipientId);

  long countByRecipientIdAndReadFalse(Long recipientId);
}
