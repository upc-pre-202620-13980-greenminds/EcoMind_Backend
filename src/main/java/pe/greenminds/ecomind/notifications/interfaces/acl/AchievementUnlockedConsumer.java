package pe.greenminds.ecomind.notifications.interfaces.acl;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementUnlockedIntegrationEvent;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.GamificationServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

/**
 * Listens to Gamification: the user is told the name of the individual achievement they earned
 * (ACHIEVEMENTS_AND_BADGES). The award id makes a repeated delivery harmless.
 */
@Component
public class AchievementUnlockedConsumer {

  private final GeneratedNotificationSender sender;
  private final GamificationServiceClient gamificationServiceClient;

  public AchievementUnlockedConsumer(
      GeneratedNotificationSender sender, GamificationServiceClient gamificationServiceClient) {
    this.sender = sender;
    this.gamificationServiceClient = gamificationServiceClient;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(AchievementUnlockedIntegrationEvent event) {
    gamificationServiceClient
        .findAchievementName(event.achievementId())
        .ifPresent(
            name ->
                sender.send(
                    List.of(event.userId()),
                    NotificationCategory.ACHIEVEMENTS_AND_BADGES,
                    NotificationType.ACHIEVEMENT_UNLOCKED,
                    name,
                    null,
                    "ACHIEVEMENT:" + event.awardId()));
  }
}
