package pe.greenminds.ecomind.notifications.application.internal.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.learning.interfaces.acl.LearningContextFacade.PendingMaterial;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.LearningServiceClient;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

/**
 * HU-025: once a day it reminds the favorites that were not opened two days after being saved.
 * Each favorite is reminded only once, even if the task runs again.
 */
@Service
public class LearningReminderService {

  /** A favorite becomes pending two days after it is saved. */
  static final Duration PENDING_AFTER = Duration.ofDays(2);

  /** Older favorites are left out, so the first run does not remind a whole history at once. */
  static final Duration LOOK_BACK = Duration.ofDays(7);

  private final LearningServiceClient learningServiceClient;
  private final GeneratedNotificationSender sender;

  public LearningReminderService(
      LearningServiceClient learningServiceClient, GeneratedNotificationSender sender) {
    this.learningServiceClient = learningServiceClient;
    this.sender = sender;
  }

  @Scheduled(cron = "${notifications.learning-reminders.cron:0 0 9 * * *}", zone = "America/Lima")
  public void remindPendingMaterials() {
    remindPendingMaterials(Instant.now());
  }

  /** Separated from the schedule so the tests can choose the current instant. */
  public void remindPendingMaterials(Instant now) {
    Instant savedBefore = now.minus(PENDING_AFTER);
    Instant savedAfter = savedBefore.minus(LOOK_BACK);
    for (PendingMaterial material :
        learningServiceClient.findPendingMaterials(savedAfter, savedBefore)) {
      sender.send(
          List.of(material.userId()),
          NotificationCategory.LEARNING_REMINDERS,
          NotificationType.LEARNING_REMINDER,
          shorten(material.title()),
          String.valueOf(material.durationMinutes()),
          "LEARNING_REMINDER:" + material.favoriteId());
    }
  }

  // A material title can be longer than what a notification keeps
  private static String shorten(String title) {
    return title.length() <= Notification.SUBJECT_MAX_LENGTH
        ? title
        : title.substring(0, Notification.SUBJECT_MAX_LENGTH);
  }
}
