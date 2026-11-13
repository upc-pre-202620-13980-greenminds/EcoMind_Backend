package pe.greenminds.ecomind.notifications.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Stores a notification. The inbox always reads the notifications of one recipient by date, so
 * that pair has an index.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "notifications",
    indexes =
        @Index(name = "ix_notifications_recipient_date", columnList = "recipient_id, created_at"),
    // The same source never notifies a recipient twice; rows without a key do not take part
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_notifications_recipient_source",
            columnNames = {"recipient_id", "source_key"}))
public class NotificationPersistenceEntity extends AuditableAbstractPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long recipientId;

  @Column(nullable = false, length = 40)
  private String category;

  @Column(nullable = false, length = 40)
  private String type;

  @Column(length = 120)
  private String title;

  @Column(length = 500)
  private String message;

  @Column(length = 150)
  private String actorName;

  @Column(length = 150)
  private String detail;

  @Column(length = 120)
  private String sourceKey;

  // "is_read" instead of "read", which some databases treat as a reserved word
  @Column(name = "is_read", nullable = false)
  private boolean read;
}
