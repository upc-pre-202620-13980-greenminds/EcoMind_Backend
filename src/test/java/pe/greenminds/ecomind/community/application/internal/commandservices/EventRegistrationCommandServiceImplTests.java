package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.*;
import pe.greenminds.ecomind.community.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class EventRegistrationCommandServiceImplTests {
  @Mock EventRepository events;
  @Mock EventRegistrationRepository registrations;
  @Mock CommunityActorGateway actors;
  EventRegistrationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EventRegistrationCommandServiceImpl(events, registrations, actors);
  }

  void event(int capacity) {
    when(events.findById(1L))
        .thenReturn(
            Optional.of(
                new Event(
                    1L,
                    2L,
                    10L,
                    "Cleanup",
                    null,
                    LocalDate.of(2026, 11, 1),
                    LocalTime.NOON,
                    "Lima",
                    null,
                    null,
                    capacity,
                    null)));
  }

  EventRegistration registration(EventRegistrationStatus status) {
    return new EventRegistration(5L, 1L, 20L, EventRegistrationType.INDIVIDUAL, null, 1, status);
  }

  RegisterForEventCommand individual() {
    return new RegisterForEventCommand(1L, 20L, EventRegistrationType.INDIVIDUAL, null);
  }

  @Test
  void missingEventIsRejected() {
    assertTrue(service.handle(individual()).isFailure());
    verifyNoInteractions(registrations);
  }

  @Test
  void activeDuplicateIsRejected() {
    event(3);
    when(registrations.findByEventIdAndUserId(1L, 20L))
        .thenReturn(Optional.of(registration(EventRegistrationStatus.REGISTERED)));
    assertTrue(service.handle(individual()).isFailure());
    verify(registrations, never()).save(any());
  }

  @Test
  void cancelledRegistrationDoesNotConsumeCapacity() {
    event(1);
    when(registrations.findByEventId(1L))
        .thenReturn(List.of(registration(EventRegistrationStatus.CANCELLED)));
    when(registrations.save(any())).thenAnswer(c -> c.getArgument(0));
    assertEquals(1, service.handle(individual()).toOptional().orElseThrow().participantCount());
  }

  @Test
  void cancelledRegistrationIsReactivatedWithoutViolatingUniqueMembership() {
    event(1);
    when(registrations.findByEventIdAndUserId(1L, 20L))
        .thenReturn(Optional.of(registration(EventRegistrationStatus.CANCELLED)));
    when(registrations.save(any())).thenAnswer(c -> c.getArgument(0));
    var result = service.handle(individual()).toOptional().orElseThrow();
    assertEquals(5L, result.id());
    assertEquals(EventRegistrationStatus.REGISTERED, result.status());
  }

  @Test
  void familySizeComesFromUsersContext() {
    event(4);
    when(actors.requireFamilyParentAndCount(20L, 8L)).thenReturn(3);
    when(registrations.save(any())).thenAnswer(c -> c.getArgument(0));
    var result =
        service
            .handle(new RegisterForEventCommand(1L, 20L, EventRegistrationType.FAMILY, 8L))
            .toOptional()
            .orElseThrow();
    assertEquals(3, result.participantCount());
  }

  @Test
  void wholeFamilyMustFitInCapacity() {
    event(3);
    when(actors.requireFamilyParentAndCount(20L, 8L)).thenReturn(3);
    when(registrations.findByEventId(1L))
        .thenReturn(List.of(registration(EventRegistrationStatus.REGISTERED)));
    assertTrue(
        service
            .handle(new RegisterForEventCommand(1L, 20L, EventRegistrationType.FAMILY, 8L))
            .isFailure());
    verify(registrations, never()).save(any());
  }

  @Test
  void individualCannotSupplyFamilyId() {
    event(3);
    assertTrue(
        service
            .handle(new RegisterForEventCommand(1L, 20L, EventRegistrationType.INDIVIDUAL, 8L))
            .isFailure());
    verify(registrations, never()).save(any());
  }

  @Test
  void familyIdIsRequired() {
    event(3);
    assertTrue(
        service
            .handle(new RegisterForEventCommand(1L, 20L, EventRegistrationType.FAMILY, null))
            .isFailure());
    verifyNoInteractions(actors);
  }

  @Test
  void unrelatedParentCannotRegisterFamily() {
    event(3);
    doThrow(new SecurityException("Not a family parent"))
        .when(actors)
        .requireFamilyParentAndCount(20L, 8L);
    assertTrue(
        service
            .handle(new RegisterForEventCommand(1L, 20L, EventRegistrationType.FAMILY, 8L))
            .isFailure());
    verify(registrations, never()).save(any());
  }

  @Test
  void cancellationRequiresOwner() {
    when(registrations.findById(5L))
        .thenReturn(Optional.of(registration(EventRegistrationStatus.REGISTERED)));
    assertTrue(service.handle(new CancelEventRegistrationCommand(1L, 5L, 30L)).isFailure());
    verify(registrations, never()).save(any());
  }

  @Test
  void cancellationRequiresMatchingEvent() {
    when(registrations.findById(5L))
        .thenReturn(Optional.of(registration(EventRegistrationStatus.REGISTERED)));
    assertTrue(service.handle(new CancelEventRegistrationCommand(2L, 5L, 20L)).isFailure());
    verify(registrations, never()).save(any());
  }

  @Test
  void ownerCanCancel() {
    when(registrations.findById(5L))
        .thenReturn(Optional.of(registration(EventRegistrationStatus.REGISTERED)));
    assertTrue(service.handle(new CancelEventRegistrationCommand(1L, 5L, 20L)).isSuccess());
    verify(registrations).save(argThat(r -> r.status() == EventRegistrationStatus.CANCELLED));
  }

  @Test
  void repeatedCancellationIsRejected() {
    when(registrations.findById(5L))
        .thenReturn(Optional.of(registration(EventRegistrationStatus.CANCELLED)));
    assertTrue(service.handle(new CancelEventRegistrationCommand(1L, 5L, 20L)).isFailure());
    verify(registrations, never()).save(any());
  }
}
