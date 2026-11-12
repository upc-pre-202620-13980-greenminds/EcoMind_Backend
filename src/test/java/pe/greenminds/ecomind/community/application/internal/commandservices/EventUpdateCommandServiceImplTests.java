package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.community.application.commandservices.EventLifecycleService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.commands.UpdateEventCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;
import pe.greenminds.ecomind.community.domain.repositories.PostRepository;

@ExtendWith(MockitoExtension.class)
class EventUpdateCommandServiceImplTests {
  @Mock EventRepository events;
  @Mock CommunityRepository communities;
  @Mock CommunityMembershipRepository memberships;
  @Mock EventRegistrationRepository registrations;
  @Mock PostRepository posts;
  @Mock CommunityActorGateway actors;
  @Mock ApplicationEventPublisher publisher;
  @Mock EventLifecycleService lifecycle;
  EventCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EventCommandServiceImpl(
        events, communities, memberships, registrations, posts, actors, publisher, lifecycle);
  }

  private Event event(LocalDate date, int availablePlaces) {
    return new Event(2L, 1L, 20L, "Cleanup", "Park", date, LocalTime.NOON,
        "Lima", -12.0, -77.0, availablePlaces, null);
  }

  private UpdateEventCommand update(Long requestedBy, LocalDate date, Integer capacity) {
    return new UpdateEventCommand(2L, requestedBy, "Updated cleanup", null, date,
        null, null, null, null, capacity, null);
  }

  @Test
  void missingEventAndNonCreatorCannotUpdate() {
    assertTrue(service.handle(update(20L, null, null)).isFailure());
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event(LocalDate.now().plusDays(2), 4)));
    assertTrue(service.handle(update(30L, null, null)).isFailure());
    verify(events, never()).save(any());
    verifyNoInteractions(registrations);
  }

  @Test
  void closedEventRejectsScheduleChangesButStillAllowsNonScheduleDetails() {
    Event expired = event(LocalDate.now().minusDays(2), 4);
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(expired));
    assertTrue(service.handle(update(20L, LocalDate.now().plusDays(3), null)).isFailure());
    verify(events, never()).save(any());

    when(events.save(any())).thenAnswer(call -> call.getArgument(0));
    var result = service.handle(update(20L, null, null)).toOptional().orElseThrow();
    assertEquals("Updated cleanup", result.getName());
    assertEquals(expired.getDate(), result.getDate());
    verify(events).save(any());
  }

  @Test
  void scheduleCannotBeMovedToAnAlreadyExpiredChatWindow() {
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event(LocalDate.now().plusDays(2), 4)));
    assertTrue(service.handle(update(20L, LocalDate.now().minusDays(3), null)).isFailure());
    verify(events, never()).save(any());
    verifyNoInteractions(registrations);
  }

  @Test
  void invalidCoordinatesReturnValidationFailureWithoutSaving() {
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event(LocalDate.now().plusDays(2), 4)));
    var invalid = new UpdateEventCommand(2L, 20L, null, null, null, null,
        null, 91.0, -77.0, null, null);
    assertTrue(service.handle(invalid).isFailure());
    verify(events, never()).save(any());
  }

  @Test
  void capacityCannotDropBelowActiveFamilyAndIndividualParticipants() {
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event(LocalDate.now().plusDays(2), 8)));
    when(registrations.findByEventId(2L)).thenReturn(List.of(
        new EventRegistration(1L, 2L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.REGISTERED),
        new EventRegistration(2L, 2L, 30L, EventRegistrationType.FAMILY, 5L, 3,
            EventRegistrationStatus.REGISTERED),
        new EventRegistration(3L, 2L, 40L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.CANCELLED)));

    assertTrue(service.handle(update(20L, null, 3)).isFailure());
    verify(events, never()).save(any());
  }

  @Test
  void capacityUpdateReservesExistingParticipantsFromNewCapacity() {
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event(LocalDate.now().plusDays(2), 8)));
    when(registrations.findByEventId(2L)).thenReturn(List.of(
        new EventRegistration(1L, 2L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.REGISTERED),
        new EventRegistration(2L, 2L, 30L, EventRegistrationType.FAMILY, 5L, 2,
            EventRegistrationStatus.REGISTERED)));
    when(events.save(any())).thenAnswer(call -> call.getArgument(0));

    Event updated = service.handle(update(20L, null, 5)).toOptional().orElseThrow();

    assertEquals(2, updated.getCapacity());
    verify(events).save(argThat(saved -> saved.getCapacity() == 2));
  }

  @Test
  void capacityUpdateWithNoActiveParticipantsDoesNotReservePlaces() {
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event(LocalDate.now().plusDays(2), 8)));
    when(registrations.findByEventId(2L)).thenReturn(List.of(
        new EventRegistration(1L, 2L, 30L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.CANCELLED)));
    when(events.save(any())).thenAnswer(call -> call.getArgument(0));

    Event updated = service.handle(update(20L, null, 5)).toOptional().orElseThrow();

    assertEquals(5, updated.getCapacity());
    verify(events).save(argThat(saved -> saved.getCapacity() == 5));
  }
}
