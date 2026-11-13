package pe.greenminds.ecomind.notifications.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.community.application.internal.eventhandlers.CommunityNoticeEventHandler;
import pe.greenminds.ecomind.community.domain.model.events.CommunityGoalCompletedEvent;
import pe.greenminds.ecomind.community.domain.model.events.EventCreatedEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventPublishedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalReachedIntegrationEvent;
import pe.greenminds.ecomind.users.application.internal.eventhandlers.FriendRequestAcceptedEventHandler;
import pe.greenminds.ecomind.users.domain.model.events.FriendRequestAcceptedEvent;
import pe.greenminds.ecomind.users.interfaces.acl.events.FriendRequestAcceptedIntegrationEvent;

/** The domain events of Users and Community become the public events that Notifications reads. */
@ExtendWith(MockitoExtension.class)
class IntegrationEventPublishersTests {

  @Mock
  private ApplicationEventPublisher publisher;

  @Test
  void anAcceptedFriendRequestIsPublishedWithTheSameIdEveryTime() {
    var handler = new FriendRequestAcceptedEventHandler(publisher);
    var event = new FriendRequestAcceptedEvent(10L, 1L, 2L, Instant.now());

    handler.on(event);
    handler.on(event);

    var published = ArgumentCaptor.forClass(FriendRequestAcceptedIntegrationEvent.class);
    verify(publisher, times(2)).publishEvent(published.capture());
    assertThat(published.getAllValues().get(0).eventId())
        .isEqualTo(published.getAllValues().get(1).eventId());
    assertThat(published.getValue().requesterId()).isEqualTo(1L);
  }

  @Test
  void aNewCommunityEventIsPublishedAsCreated() {
    var handler = new CommunityNoticeEventHandler(publisher);

    handler.on(new EventCreatedEvent(20L, 7L, 1L, "Park cleanup"));

    var published = ArgumentCaptor.forClass(CommunityEventPublishedIntegrationEvent.class);
    verify(publisher).publishEvent(published.capture());
    assertThat(published.getValue().action())
        .isEqualTo(CommunityEventPublishedIntegrationEvent.Action.CREATED);
    assertThat(published.getValue().name()).isEqualTo("Park cleanup");
  }

  @Test
  void aCompletedGoalIsPublishedAsANoticeWithoutReward() {
    var handler = new CommunityNoticeEventHandler(publisher);

    handler.on(new CommunityGoalCompletedEvent(40L, 7L, "Save energy"));

    var published = ArgumentCaptor.forClass(CommunityGoalReachedIntegrationEvent.class);
    verify(publisher).publishEvent(published.capture());
    assertThat(published.getValue().goalId()).isEqualTo(40L);
    assertThat(published.getValue().title()).isEqualTo("Save energy");
  }
}
