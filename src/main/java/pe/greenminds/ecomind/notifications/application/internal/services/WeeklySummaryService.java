package pe.greenminds.ecomind.notifications.application.internal.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.GamificationServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

/**
 * Every Monday it tells each user the EcoPoints earned in the last seven days and the current
 * streak (WEEKLY_PROGRESS_SUMMARY). Users who earned nothing are not notified, and the week is
 * part of the key, so running the task again in the same week does not repeat it.
 */
@Service
public class WeeklySummaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeeklySummaryService.class);
  private static final ZoneId LIMA = ZoneId.of("America/Lima");
  private static final Duration WEEK = Duration.ofDays(7);

  private final UsersServiceClient usersServiceClient;
  private final GamificationServiceClient gamificationServiceClient;
  private final GeneratedNotificationSender sender;

  public WeeklySummaryService(
      UsersServiceClient usersServiceClient,
      GamificationServiceClient gamificationServiceClient,
      GeneratedNotificationSender sender) {
    this.usersServiceClient = usersServiceClient;
    this.gamificationServiceClient = gamificationServiceClient;
    this.sender = sender;
  }

  @Scheduled(cron = "${notifications.weekly-summary.cron:0 0 8 * * MON}", zone = "America/Lima")
  public void sendWeeklySummaries() {
    sendWeeklySummaries(Instant.now());
  }

  /** Separated from the schedule so the tests can choose the current instant. */
  public void sendWeeklySummaries(Instant now) {
    Instant from = now.minus(WEEK);
    String sourceKey = "WEEKLY_SUMMARY:" + weekOf(now);
    for (Long userId : usersServiceClient.getAllUserIds()) {
      try {
        long ecopoints = gamificationServiceClient.getEcopointsEarned(userId, from, now);
        if (ecopoints <= 0) {
          continue;
        }
        int streak = gamificationServiceClient.getCurrentStreak(userId);
        sender.send(
            List.of(userId),
            NotificationCategory.WEEKLY_PROGRESS_SUMMARY,
            NotificationType.WEEKLY_SUMMARY,
            String.valueOf(ecopoints),
            String.valueOf(streak),
            sourceKey);
      } catch (RuntimeException exception) {
        // One user without progress data must not stop the summaries of the others
        LOGGER.warn("Weekly summary for user {} was not created", userId);
      }
    }
  }

  // ISO week in Lima, such as 2026-W46
  private static String weekOf(Instant now) {
    LocalDate date = now.atZone(LIMA).toLocalDate();
    return date.get(IsoFields.WEEK_BASED_YEAR) + "-W" + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
  }
}
