package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.events.EventCreatedEvent;
import pe.greenminds.ecomind.community.domain.model.valueobjects.*;
import pe.greenminds.ecomind.community.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class EventCommandServiceImplTests {
  @Mock EventRepository events;
  @Mock CommunityRepository communities;
  @Mock CommunityMembershipRepository memberships;
  @Mock EventRegistrationRepository registrations;
  @Mock PostRepository posts;
  @Mock CommunityActorGateway actors;
  @Mock ApplicationEventPublisher publisher;
  @InjectMocks EventCommandServiceImpl service;

  CreateEventCommand create() {
    return new CreateEventCommand(
        1L,
        20L,
        "Cleanup",
        "Park",
        LocalDate.now().plusDays(2),
        LocalTime.NOON,
        "Lima",
        -12.0,
        -77.0,
        10,
        null);
  }

  Event event() {
    return new Event(
        2L,
        1L,
        20L,
        "Cleanup",
        "Park",
        LocalDate.now().plusDays(2),
        LocalTime.NOON,
        "Lima",
        -12.0,
        -77.0,
        10,
        null);
  }

  @Test
  void creationRequiresExistingCommunity() {
    assertTrue(service.handle(create()).isFailure());
    verifyNoInteractions(events, registrations, publisher);
  }

  @Test
  void creationRequiresParent() {
    when(communities.existsById(1L)).thenReturn(true);
    doThrow(new SecurityException("Parent required")).when(actors).requireParent(20L);
    assertTrue(service.handle(create()).isFailure());
    verifyNoInteractions(events, registrations, publisher);
  }

  @Test
  void creatorReceivesPlaceAndCommunityAnnouncement() {
    when(communities.existsById(1L)).thenReturn(true);
    when(events.save(any())).thenReturn(event());
    assertTrue(service.handle(create()).isSuccess());
    verify(registrations)
        .save(
            argThat(
                r ->
                    r.eventId().equals(2L) && r.userId().equals(20L) && r.participantCount() == 1));
    verify(posts).save(any());
    verify(publisher).publishEvent(any(EventCreatedEvent.class));
  }

  @Test
  void missingEventCannotBeDeleted() {
    assertTrue(service.handle(new DeleteEventCommand(2L, 20L)).isFailure());
    verify(events, never()).delete(any());
  }

  @ParameterizedTest
  @ValueSource(longs = {20, 30})
  void creatorOrAdministratorCanDelete(long actor) {
    var e = event();
    when(events.findById(2L)).thenReturn(Optional.of(e));
    if (actor == 30)
      when(memberships.findByCommunityIdAndUserId(1L, actor))
          .thenReturn(Optional.of(new CommunityMembership(4L, 1L, actor, CommunityRole.ADMIN)));
    assertTrue(service.handle(new DeleteEventCommand(2L, actor)).isSuccess());
    verify(events).delete(e);
  }

  @Test
  void ordinaryMemberCannotDeleteEvent() {
    when(events.findById(2L)).thenReturn(Optional.of(event()));
    when(memberships.findByCommunityIdAndUserId(1L, 30L))
        .thenReturn(Optional.of(new CommunityMembership(4L, 1L, 30L, CommunityRole.MEMBER)));
    assertTrue(service.handle(new DeleteEventCommand(2L, 30L)).isFailure());
    verify(events, never()).delete(any());
  }

  @Test
  void outsiderCannotDeleteEvent() {
    when(events.findById(2L)).thenReturn(Optional.of(event()));
    assertTrue(service.handle(new DeleteEventCommand(2L, 30L)).isFailure());
    verify(events, never()).delete(any());
  }
}
