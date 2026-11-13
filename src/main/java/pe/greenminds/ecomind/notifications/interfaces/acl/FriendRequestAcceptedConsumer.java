package pe.greenminds.ecomind.notifications.interfaces.acl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.users.domain.model.events.FriendRequestAcceptedEvent;

/**
 * Listens to Users: when a friend request is accepted, the user who sent it receives a
 * notification with the name of the friend. It is the only generator for now; Quests, Learning,
 * Gamification and Community still have to publish their own events to this context.
 */
@Component
public class FriendRequestAcceptedConsumer {

  private final NotificationCommandService notificationCommandService;
  private final UsersServiceClient usersServiceClient;

  public FriendRequestAcceptedConsumer(
      NotificationCommandService notificationCommandService,
      UsersServiceClient usersServiceClient) {
    this.notificationCommandService = notificationCommandService;
    this.usersServiceClient = usersServiceClient;
  }

  @EventListener
  public void on(FriendRequestAcceptedEvent event) {
    // Without a profile there is no name to show, so no notification is created
    usersServiceClient
        .getUserName(event.receiverId())
        .ifPresent(
            name ->
                notificationCommandService.handle(
                    new NotifyFriendRequestAcceptedCommand(
                        new RecipientId(event.requesterId()), name)));
  }
}
