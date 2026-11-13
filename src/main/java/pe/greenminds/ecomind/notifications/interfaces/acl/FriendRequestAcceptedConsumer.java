package pe.greenminds.ecomind.notifications.interfaces.acl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.users.interfaces.acl.events.FriendRequestAcceptedIntegrationEvent;

/**
 * Listens to Users: when a friend request is accepted, the user who sent it receives a
 * notification with the name of the friend. It runs after the friendship is saved, so a failure
 * here never undoes the acceptance.
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

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void on(FriendRequestAcceptedIntegrationEvent event) {
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
