package pe.greenminds.ecomind.community.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.*;
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
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventChatAccessServiceImplTests {
  @Mock EventRepository events;
  @Mock EventRegistrationRepository registrations;
  EventChatAccessServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EventChatAccessServiceImpl(events, registrations);
  }

  @Test
  void missingEventReturnsNotFoundWithoutLookingUpRegistrations() {
    when(events.findByIdForUpdate(7L)).thenReturn(Optional.empty());
    assertTrue(service.requireParticipant(7L, 20L).isFailure());
    verifyNoInteractions(registrations);
  }

  @Test
  void outsidersAndCancelledMembersCannotAccessChat() {
    Event event = event(LocalDate.now().plusDays(2));
    when(events.findByIdForUpdate(7L)).thenReturn(Optional.of(event));
    when(registrations.findByEventId(7L)).thenReturn(List.of(
        registration(30L, EventRegistrationStatus.REGISTERED),
        registration(20L, EventRegistrationStatus.CANCELLED)));
    assertTrue(service.requireParticipant(7L, 20L).isFailure());
  }

  @Test
  void activeParticipantCanReadChatBeforeExpiry() {
    Event event = event(LocalDate.now().plusDays(2));
    when(events.findByIdForUpdate(7L)).thenReturn(Optional.of(event));
    when(registrations.findByEventId(7L)).thenReturn(List.of(
        new EventRegistration(4L, 7L, 30L, EventRegistrationType.FAMILY, 9L, 2,
            EventRegistrationStatus.REGISTERED, java.util.Set.of(20L, 30L))));
    assertEquals(event, service.requireParticipant(7L, 20L).toOptional().orElseThrow());
  }

  @Test
  void activeParticipantCannotReadChatAfterExpiry() {
    when(events.findByIdForUpdate(7L)).thenReturn(Optional.of(event(LocalDate.now().minusDays(2))));
    when(registrations.findByEventId(7L)).thenReturn(List.of(
        registration(20L, EventRegistrationStatus.REGISTERED)));
    assertTrue(service.requireParticipant(7L, 20L).isFailure());
  }

  private Event event(LocalDate date) {
    return new Event(7L, 3L, 30L, "Cleanup", null, date, LocalTime.NOON,
        "Lima", null, null, 2, null);
  }

  private EventRegistration registration(Long userId, EventRegistrationStatus status) {
    return new EventRegistration(4L, 7L, userId, EventRegistrationType.INDIVIDUAL,
        null, 1, status);
  }
}
