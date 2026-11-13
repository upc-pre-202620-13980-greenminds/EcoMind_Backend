package pe.greenminds.ecomind.notifications.interfaces.acl;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventPublishedIntegrationEvent;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.CommunityServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

/**
 * HU-018: when a community event is published or updated, only the parents are told. They are the
 * parents who belong to the community and the parents of the families of its members.
 */
@Component
public class CommunityEventConsumer {

  private final GeneratedNotificationSender sender;
  private final CommunityServiceClient communityServiceClient;
  private final UsersServiceClient usersServiceClient;

  public CommunityEventConsumer(
      GeneratedNotificationSender sender,
      CommunityServiceClient communityServiceClient,
      UsersServiceClient usersServiceClient) {
    this.sender = sender;
    this.communityServiceClient = communityServiceClient;
    this.usersServiceClient = usersServiceClient;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(CommunityEventPublishedIntegrationEvent event) {
    boolean created = event.action() == CommunityEventPublishedIntegrationEvent.Action.CREATED;
    sender.send(
        parentsToNotify(event),
        NotificationCategory.COMMUNITY_EVENTS,
        created ? NotificationType.COMMUNITY_EVENT_CREATED : NotificationType.COMMUNITY_EVENT_UPDATED,
        event.name(),
        null,
        created
            ? "COMMUNITY_EVENT_CREATED:" + event.communityEventId()
            : "COMMUNITY_EVENT_UPDATED:" + event.communityEventId() + ":" + event.eventId());
  }

  private Set<Long> parentsToNotify(CommunityEventPublishedIntegrationEvent event) {
    Set<Long> candidates = new LinkedHashSet<>();
    for (Long memberId : communityServiceClient.findMemberIds(event.communityId())) {
      candidates.addAll(usersServiceClient.getUserAndFamilyMemberIds(memberId));
    }
    // Whoever created or changed the event does not need the alert
    candidates.remove(event.authorId());
    candidates.removeIf(userId -> !usersServiceClient.isParent(userId));
    return candidates;
  }
}
