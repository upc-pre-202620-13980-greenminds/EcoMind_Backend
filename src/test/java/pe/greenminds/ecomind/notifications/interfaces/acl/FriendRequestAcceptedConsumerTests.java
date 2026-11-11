package pe.greenminds.ecomind.notifications.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.domain.model.commands.NotifyFriendRequestAcceptedCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.users.domain.model.events.FriendRequestAcceptedEvent;

@ExtendWith(MockitoExtension.class)
class FriendRequestAcceptedConsumerTests {

  private static final Long LUIS = 1L;
  private static final Long ANA = 2L;

  @Mock
  private NotificationCommandService notificationCommandService;
  @Mock
  private UsersServiceClient usersServiceClient;

  private FriendRequestAcceptedConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new FriendRequestAcceptedConsumer(notificationCommandService, usersServiceClient);
  }

  @Test
  void theRequesterIsNotifiedWithTheNameOfWhoAccepted() {
    when(usersServiceClient.getUserName(ANA)).thenReturn(Optional.of("Ana"));

    consumer.on(new FriendRequestAcceptedEvent(10L, LUIS, ANA, Instant.now()));

    var command = ArgumentCaptor.forClass(NotifyFriendRequestAcceptedCommand.class);
    verify(notificationCommandService).handle(command.capture());
    assertThat(command.getValue().requesterId()).isEqualTo(new RecipientId(LUIS));
    assertThat(command.getValue().accepterName()).isEqualTo("Ana");
  }

  @Test
  void nothingIsCreatedWhenTheAccepterHasNoProfile() {
    when(usersServiceClient.getUserName(ANA)).thenReturn(Optional.empty());

    consumer.on(new FriendRequestAcceptedEvent(10L, LUIS, ANA, Instant.now()));

    verify(notificationCommandService, never())
        .handle(any(NotifyFriendRequestAcceptedCommand.class));
  }
}
