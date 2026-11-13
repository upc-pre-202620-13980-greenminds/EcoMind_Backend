package pe.greenminds.ecomind.notifications.interfaces.acl;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalReachedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.CommunityServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

/**
 * Listens to Community (COMMUNITY_ACTIVITY): the members are told when someone shares an
 * achievement in the community and when the community completes a goal.
 */
@Component
public class CommunityActivityConsumer {

  private final GeneratedNotificationSender sender;
  private final CommunityServiceClient communityServiceClient;
  private final UsersServiceClient usersServiceClient;

  public CommunityActivityConsumer(
      GeneratedNotificationSender sender,
      CommunityServiceClient communityServiceClient,
      UsersServiceClient usersServiceClient) {
    this.sender = sender;
    this.communityServiceClient = communityServiceClient;
    this.usersServiceClient = usersServiceClient;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(PublicationCreatedIntegrationEvent event) {
    // The author already knows what they shared
    List<Long> recipients =
        communityServiceClient.findMemberIds(event.communityId()).stream()
            .filter(memberId -> !memberId.equals(event.requestedBy()))
            .toList();
    usersServiceClient
        .getUserName(event.requestedBy())
        .ifPresent(
            authorName ->
                sender.send(
                    recipients,
                    NotificationCategory.COMMUNITY_ACTIVITY,
                    NotificationType.ACHIEVEMENT_SHARED,
                    authorName,
                    null,
                    "PUBLICATION:" + event.publicationId()));
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(CommunityGoalReachedIntegrationEvent event) {
    sender.send(
        communityServiceClient.findMemberIds(event.communityId()),
        NotificationCategory.COMMUNITY_ACTIVITY,
        NotificationType.COMMUNITY_GOAL_REACHED,
        event.title(),
        null,
        "COMMUNITY_GOAL:" + event.goalId());
  }
}
