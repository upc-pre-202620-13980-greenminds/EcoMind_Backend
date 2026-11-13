package pe.greenminds.ecomind.notifications.application.internal.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.learning.interfaces.acl.LearningContextFacade.PendingMaterial;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.GamificationServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.LearningServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyUserCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationsTests {

  // A Monday at 08:00 in Lima
  private static final Instant NOW = Instant.parse("2026-11-16T13:00:00Z");

  @Mock
  private GeneratedNotificationSender sender;
  @Mock
  private LearningServiceClient learningServiceClient;
  @Mock
  private UsersServiceClient usersServiceClient;
  @Mock
  private GamificationServiceClient gamificationServiceClient;
  @Mock
  private NotificationCommandService notificationCommandService;

  @Test
  void aFavoriteNotOpenedForTwoDaysIsRemindedWithItsMinutes() {
    var service = new LearningReminderService(learningServiceClient, sender);
    Instant savedBefore = NOW.minus(LearningReminderService.PENDING_AFTER);
    when(learningServiceClient.findPendingMaterials(
            savedBefore.minus(LearningReminderService.LOOK_BACK), savedBefore))
        .thenReturn(List.of(new PendingMaterial(9L, 1L, 3L, "Recycling essentials", 10)));

    service.remindPendingMaterials(NOW);

    verify(sender)
        .send(
            List.of(1L),
            NotificationCategory.LEARNING_REMINDERS,
            NotificationType.LEARNING_REMINDER,
            "Recycling essentials",
            "10",
            "LEARNING_REMINDER:9");
  }

  @Test
  void theWeeklySummaryOnlyGoesToWhoEarnedEcoPoints() {
    var service = new WeeklySummaryService(usersServiceClient, gamificationServiceClient, sender);
    when(usersServiceClient.getAllUserIds()).thenReturn(List.of(1L, 2L));
    when(gamificationServiceClient.getEcopointsEarned(eq(1L), any(), eq(NOW))).thenReturn(120L);
    when(gamificationServiceClient.getEcopointsEarned(eq(2L), any(), eq(NOW))).thenReturn(0L);
    when(gamificationServiceClient.getCurrentStreak(1L)).thenReturn(3);

    service.sendWeeklySummaries(NOW);

    verify(sender)
        .send(
            List.of(1L),
            NotificationCategory.WEEKLY_PROGRESS_SUMMARY,
            NotificationType.WEEKLY_SUMMARY,
            "120",
            "3",
            "WEEKLY_SUMMARY:2026-W47");
    verify(sender, never())
        .send(eq(List.of(2L)), any(), any(), anyString(), anyString(), anyString());
  }

  @Test
  void runningTheWeeklySummaryAgainInTheSameWeekUsesTheSameKey() {
    var service = new WeeklySummaryService(usersServiceClient, gamificationServiceClient, sender);
    when(usersServiceClient.getAllUserIds()).thenReturn(List.of(1L));
    when(gamificationServiceClient.getEcopointsEarned(eq(1L), any(), any())).thenReturn(50L);
    when(gamificationServiceClient.getCurrentStreak(1L)).thenReturn(1);

    service.sendWeeklySummaries(NOW);
    service.sendWeeklySummaries(NOW.plusSeconds(3600));

    ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
    verify(sender, times(2))
        .send(any(), any(), any(), anyString(), anyString(), keys.capture());
    assertThat(keys.getAllValues()).containsOnly("WEEKLY_SUMMARY:2026-W47");
  }

  @Test
  void aFailedRecipientDoesNotStopTheOthers() {
    var realSender = new GeneratedNotificationSender(notificationCommandService);
    when(notificationCommandService.handle(any(NotifyUserCommand.class)))
        .thenThrow(new IllegalStateException("duplicate key"))
        .thenReturn(null);

    realSender.send(
        List.of(1L, 2L),
        NotificationCategory.COMMUNITY_ACTIVITY,
        NotificationType.COMMUNITY_GOAL_REACHED,
        "Save energy",
        null,
        "COMMUNITY_GOAL:40");

    ArgumentCaptor<NotifyUserCommand> commands = ArgumentCaptor.forClass(NotifyUserCommand.class);
    verify(notificationCommandService, times(2)).handle(commands.capture());
    assertThat(commands.getAllValues().get(1).recipientId()).isEqualTo(new RecipientId(2L));
  }
}
