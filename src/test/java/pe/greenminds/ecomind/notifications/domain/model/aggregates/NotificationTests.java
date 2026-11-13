package pe.greenminds.ecomind.notifications.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

class NotificationTests {

  private static final RecipientId LUIS = new RecipientId(1L);
  private static final RecipientId ANA = new RecipientId(2L);

  @Test
  void aGeneralNotificationStartsUnreadWithItsTrimmedTexts() {
    Notification notification =
        Notification.general(
            LUIS, NotificationCategory.LEARNING_REMINDERS, " Water reminder ", " Review it. ");

    assertThat(notification.getType()).isEqualTo(NotificationType.GENERAL);
    assertThat(notification.getTitle()).isEqualTo("Water reminder");
    assertThat(notification.getMessage()).isEqualTo("Review it.");
    assertThat(notification.isRead()).isFalse();
  }

  @Test
  void aGeneralNotificationNeedsATitle() {
    assertThatThrownBy(
            () -> Notification.general(LUIS, NotificationCategory.LEARNING_REMINDERS, " ", "Text"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aGeneralNotificationCannotHaveAMessageLongerThanTheLimit() {
    String longMessage = "a".repeat(Notification.MESSAGE_MAX_LENGTH + 1);

    assertThatThrownBy(
            () ->
                Notification.general(
                    LUIS, NotificationCategory.LEARNING_REMINDERS, "Title", longMessage))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void anAcceptedFriendRequestIsACommunityActivityForTheRequester() {
    Notification notification = Notification.friendRequestAccepted(LUIS, "Ana");

    assertThat(notification.getCategory()).isEqualTo(NotificationCategory.COMMUNITY_ACTIVITY);
    assertThat(notification.getType()).isEqualTo(NotificationType.FRIEND_REQUEST_ACCEPTED);
    assertThat(notification.getActorName()).isEqualTo("Ana");
    assertThat(notification.belongsTo(LUIS)).isTrue();
    assertThat(notification.belongsTo(ANA)).isFalse();
  }

  @Test
  void markingAsReadChangesTheState() {
    Notification notification = Notification.friendRequestAccepted(LUIS, "Ana");

    notification.markAsRead();

    assertThat(notification.isRead()).isTrue();
  }
}
