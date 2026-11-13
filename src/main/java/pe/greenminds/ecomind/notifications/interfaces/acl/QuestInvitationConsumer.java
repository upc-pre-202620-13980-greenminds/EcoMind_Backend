package pe.greenminds.ecomind.notifications.interfaces.acl;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.quests.interfaces.acl.events.CollaborativeQuestInvitationSentIntegrationEvent;

/**
 * Listens to Quests: the invited user is told who invited them to a collaborative quest
 * (NEW_AND_DAILY_QUESTS). It runs after the invitation is saved.
 */
@Component
public class QuestInvitationConsumer {

  private final GeneratedNotificationSender sender;
  private final UsersServiceClient usersServiceClient;

  public QuestInvitationConsumer(
      GeneratedNotificationSender sender, UsersServiceClient usersServiceClient) {
    this.sender = sender;
    this.usersServiceClient = usersServiceClient;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(CollaborativeQuestInvitationSentIntegrationEvent event) {
    usersServiceClient
        .getUserName(event.ownerUserId())
        .ifPresent(
            ownerName ->
                sender.send(
                    List.of(event.invitedUserId()),
                    NotificationCategory.NEW_AND_DAILY_QUESTS,
                    NotificationType.QUEST_INVITATION,
                    ownerName,
                    null,
                    // Quests gives the same event id to the same invitation
                    "QUEST_INVITATION:" + event.eventId()));
  }
}
