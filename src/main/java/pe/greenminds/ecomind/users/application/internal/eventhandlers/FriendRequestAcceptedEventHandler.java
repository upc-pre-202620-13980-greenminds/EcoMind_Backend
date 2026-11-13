package pe.greenminds.ecomind.users.application.internal.eventhandlers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.users.domain.model.events.FriendRequestAcceptedEvent;
import pe.greenminds.ecomind.users.interfaces.acl.events.FriendRequestAcceptedIntegrationEvent;

/**
 * Turns the domain event of Users into its public integration event, so the other bounded
 * contexts do not depend on the domain model of Users.
 */
@Component
public class FriendRequestAcceptedEventHandler {

  private final ApplicationEventPublisher eventPublisher;

  public FriendRequestAcceptedEventHandler(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @EventListener
  public void on(FriendRequestAcceptedEvent event) {
    UUID eventId =
        UUID.nameUUIDFromBytes(
            ("friend-request-accepted:" + event.friendshipId()).getBytes(StandardCharsets.UTF_8));
    eventPublisher.publishEvent(
        new FriendRequestAcceptedIntegrationEvent(
            eventId,
            event.friendshipId(),
            event.requesterId(),
            event.receiverId(),
            event.occurredAt()));
  }
}
