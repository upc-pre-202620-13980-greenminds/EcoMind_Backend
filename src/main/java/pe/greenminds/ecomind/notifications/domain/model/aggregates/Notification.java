package pe.greenminds.ecomind.notifications.domain.model.aggregates;

import java.time.Instant;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

/**
 * Message shown in the notification inbox of a user (TS-016). Only its recipient can read it or
 * mark it as read.
 */
public class Notification {

  public static final int TITLE_MAX_LENGTH = 120;
  public static final int MESSAGE_MAX_LENGTH = 500;

  private final Long id;
  private final RecipientId recipientId;
  private final NotificationCategory category;
  private final NotificationType type;
  private final String title;
  private final String message;
  private final String actorName;
  private boolean read;
  private final Instant createdAt;

  /** Rebuilds a notification that already exists; id and createdAt are null until persisted. */
  public Notification(
      Long id,
      RecipientId recipientId,
      NotificationCategory category,
      NotificationType type,
      String title,
      String message,
      String actorName,
      boolean read,
      Instant createdAt) {
    this.id = id;
    this.recipientId = recipientId;
    this.category = category;
    this.type = type;
    this.title = title;
    this.message = message;
    this.actorName = actorName;
    this.read = read;
    this.createdAt = createdAt;
  }

  /** A notification with its own texts, such as a reminder the user creates. */
  public static Notification general(
      RecipientId recipientId, NotificationCategory category, String title, String message) {
    String cleanTitle = requireText(title, TITLE_MAX_LENGTH, "Notification title");
    String cleanMessage = requireText(message, MESSAGE_MAX_LENGTH, "Notification message");
    return new Notification(
        null, recipientId, category, NotificationType.GENERAL, cleanTitle, cleanMessage, null,
        false, null);
  }

  /** Tells the requester that the other user accepted the friend request. */
  public static Notification friendRequestAccepted(RecipientId requesterId, String accepterName) {
    String name = requireText(accepterName, 150, "Name of the user who accepted");
    return new Notification(
        null, requesterId, NotificationCategory.COMMUNITY_ACTIVITY,
        NotificationType.FRIEND_REQUEST_ACCEPTED, null, null, name, false, null);
  }

  public void markAsRead() {
    this.read = true;
  }

  public boolean belongsTo(RecipientId userId) {
    return recipientId.equals(userId);
  }

  private static String requireText(String value, int maxLength, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    String trimmed = value.trim();
    if (trimmed.length() > maxLength) {
      throw new IllegalArgumentException(field + " must have up to " + maxLength + " characters");
    }
    return trimmed;
  }

  public Long getId() {
    return id;
  }

  public RecipientId getRecipientId() {
    return recipientId;
  }

  public NotificationCategory getCategory() {
    return category;
  }

  public NotificationType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getActorName() {
    return actorName;
  }

  public boolean isRead() {
    return read;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
