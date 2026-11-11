package pe.greenminds.ecomind.notifications.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.commands.CreateNotificationCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkAllNotificationsAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceImplTests {

  private static final RecipientId LUIS = new RecipientId(1L);
  private static final RecipientId ANA = new RecipientId(2L);
  private static final Long NOTIFICATION_ID = 30L;

  @Mock
  private NotificationRepository notificationRepository;

  private NotificationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new NotificationCommandServiceImpl(notificationRepository);
  }

  @Test
  void createNotificationSavesItForTheRecipient() {
    when(notificationRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result =
        service.handle(
            new CreateNotificationCommand(
                LUIS, NotificationCategory.LEARNING_REMINDERS, "Water reminder", "Review it."));

    Notification notification = result.toOptional().orElseThrow();
    assertThat(notification.getRecipientId()).isEqualTo(LUIS);
    assertThat(notification.getType()).isEqualTo(NotificationType.GENERAL);
  }

  @Test
  void createNotificationWithoutTitleFails() {
    var result =
        service.handle(
            new CreateNotificationCommand(
                LUIS, NotificationCategory.LEARNING_REMINDERS, " ", "Review it."));

    assertThat(result.isFailure()).isTrue();
    verify(notificationRepository, never()).save(any());
  }

  @Test
  void anAcceptedFriendRequestIsSavedForTheRequester() {
    when(notificationRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new NotifyFriendRequestAcceptedCommand(LUIS, "Ana"));

    Notification notification = result.toOptional().orElseThrow();
    assertThat(notification.getRecipientId()).isEqualTo(LUIS);
    assertThat(notification.getType()).isEqualTo(NotificationType.FRIEND_REQUEST_ACCEPTED);
  }

  @Test
  void theRecipientCanMarkTheirNotificationAsRead() {
    when(notificationRepository.findById(NOTIFICATION_ID))
        .thenReturn(Optional.of(unreadNotificationOf(LUIS)));
    when(notificationRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new MarkNotificationAsReadCommand(LUIS, NOTIFICATION_ID));

    assertThat(result.toOptional().orElseThrow().isRead()).isTrue();
  }

  @Test
  void anotherUserCannotMarkTheNotificationAsRead() {
    when(notificationRepository.findById(NOTIFICATION_ID))
        .thenReturn(Optional.of(unreadNotificationOf(LUIS)));

    var result = service.handle(new MarkNotificationAsReadCommand(ANA, NOTIFICATION_ID));

    assertThat(errorCodeOf(result)).isEqualTo("NOTIFICATION_ACCESS_FORBIDDEN");
    verify(notificationRepository, never()).save(any());
  }

  @Test
  void markingANotificationThatDoesNotExistFails() {
    when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

    var result = service.handle(new MarkNotificationAsReadCommand(LUIS, NOTIFICATION_ID));

    assertThat(errorCodeOf(result)).isEqualTo("NOTIFICATION_NOT_FOUND");
  }

  @Test
  void markAllAsReadChangesEveryUnreadNotification() {
    Notification first = unreadNotificationOf(LUIS);
    Notification second = unreadNotificationOf(LUIS);
    when(notificationRepository.findUnreadByRecipient(LUIS)).thenReturn(List.of(first, second));

    var result = service.handle(new MarkAllNotificationsAsReadCommand(LUIS));

    assertThat(result.toOptional()).contains(2);
    assertThat(first.isRead()).isTrue();
    assertThat(second.isRead()).isTrue();
    verify(notificationRepository, times(2)).save(any());
  }

  private static Notification unreadNotificationOf(RecipientId recipient) {
    return new Notification(
        NOTIFICATION_ID, recipient, NotificationCategory.COMMUNITY_ACTIVITY,
        NotificationType.FRIEND_REQUEST_ACCEPTED, null, null, "Ana", false, Instant.now());
  }

  private static String errorCodeOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error().code();
  }
}
