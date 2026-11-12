package pe.greenminds.ecomind.notifications.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository;
import pe.greenminds.ecomind.notifications.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;
import pe.greenminds.ecomind.notifications.infrastructure.persistence.jpa.repositories.NotificationPersistenceRepository;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

  private final NotificationPersistenceRepository persistenceRepository;

  public NotificationRepositoryImpl(NotificationPersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
  }

  @Override
  public Notification save(Notification notification) {
    NotificationPersistenceEntity entity;
    if (notification.getId() == null) {
      entity = new NotificationPersistenceEntity();
      entity.setRecipientId(notification.getRecipientId().value());
      entity.setCategory(notification.getCategory().name());
      entity.setType(notification.getType().name());
      entity.setTitle(notification.getTitle());
      entity.setMessage(notification.getMessage());
      entity.setActorName(notification.getActorName());
    } else {
      entity = persistenceRepository.findById(notification.getId()).orElseThrow();
    }
    // Only the read state changes after a notification is created
    entity.setRead(notification.isRead());
    return toDomain(persistenceRepository.saveAndFlush(entity));
  }

  @Override
  public Optional<Notification> findById(Long notificationId) {
    return persistenceRepository.findById(notificationId).map(NotificationRepositoryImpl::toDomain);
  }

  @Override
  public List<Notification> findByRecipient(RecipientId recipientId) {
    return persistenceRepository
        .findByRecipientIdOrderByCreatedAtDescIdDesc(recipientId.value())
        .stream()
        .map(NotificationRepositoryImpl::toDomain)
        .toList();
  }

  @Override
  public List<Notification> findUnreadByRecipient(RecipientId recipientId) {
    return persistenceRepository.findByRecipientIdAndReadFalse(recipientId.value()).stream()
        .map(NotificationRepositoryImpl::toDomain)
        .toList();
  }

  @Override
  public long countUnreadByRecipient(RecipientId recipientId) {
    return persistenceRepository.countByRecipientIdAndReadFalse(recipientId.value());
  }

  private static Notification toDomain(NotificationPersistenceEntity entity) {
    return new Notification(
        entity.getId(),
        new RecipientId(entity.getRecipientId()),
        NotificationCategory.valueOf(entity.getCategory()),
        NotificationType.valueOf(entity.getType()),
        entity.getTitle(),
        entity.getMessage(),
        entity.getActorName(),
        entity.isRead(),
        entity.getCreatedAt() == null ? null : entity.getCreatedAt().toInstant());
  }
}
