package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.application.commandservices.EventLifecycleService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.commands.LeaveCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.repositories.CommunityAchievementRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;
import pe.greenminds.ecomind.community.domain.repositories.PostRepository;

@ExtendWith(MockitoExtension.class)
class CommunityLeaveCommandServiceImplTests {
  @Mock CommunityRepository communities;
  @Mock CommunityMembershipRepository memberships;
  @Mock CommunityAchievementRepository achievements;
  @Mock CommunityActorGateway actors;
  @Mock EventRepository events;
  @Mock EventRegistrationRepository registrations;
  @Mock EventLifecycleService lifecycle;
  @Mock PostRepository posts;
  CommunityCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new CommunityCommandServiceImpl(
        communities, memberships, achievements, actors, events, registrations, lifecycle, posts);
  }

  @Test
  void leavingUnknownCommunityDoesNotTouchEventsOrMemberships() {
    when(memberships.findByCommunityIdAndUserId(1L, 20L)).thenReturn(Optional.empty());
    when(communities.findByIdForUpdate(1L)).thenReturn(Optional.empty());

    assertTrue(service.handle(new LeaveCommunityCommand(1L, 20L)).isFailure());

    verifyNoInteractions(events, registrations, lifecycle);
    verify(memberships, never()).delete(any());
  }

  @Test
  void leavingCommunityWithoutMembershipIsRejectedBeforeEventCleanup() {
    when(memberships.findByCommunityIdAndUserId(1L, 20L)).thenReturn(Optional.empty());
    when(communities.findByIdForUpdate(1L)).thenReturn(Optional.of(community()));

    assertTrue(service.handle(new LeaveCommunityCommand(1L, 20L)).isFailure());

    verifyNoInteractions(events, registrations, lifecycle);
    verify(memberships, never()).delete(any());
  }

  @Test
  void leavingDeletesOwnedEventsAndReleasesOwnedAndFamilyPlaces() {
    CommunityMembership membership = new CommunityMembership(5L, 1L, 20L, CommunityRole.MEMBER);
    Event ownedEvent = event(10L, 20L, 4);
    Event remainingEvent = event(11L, 99L, 3);
    EventRegistration owned = new EventRegistration(
        21L, 11L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
        EventRegistrationStatus.REGISTERED);
    EventRegistration family = new EventRegistration(
        22L, 11L, 99L, EventRegistrationType.FAMILY, 7L, 3,
        EventRegistrationStatus.REGISTERED, Set.of(20L, 21L, 99L));
    EventRegistration unrelated = new EventRegistration(
        23L, 11L, 30L, EventRegistrationType.FAMILY, 8L, 2,
        EventRegistrationStatus.REGISTERED, Set.of(30L, 31L));
    EventRegistration cancelled = new EventRegistration(
        24L, 11L, 20L, EventRegistrationType.FAMILY, 9L, 2,
        EventRegistrationStatus.CANCELLED);
    when(memberships.findByCommunityIdAndUserId(1L, 20L)).thenReturn(Optional.of(membership));
    when(communities.findByIdForUpdate(1L)).thenReturn(Optional.of(community()));
    when(events.findByCommunityIdForUpdate(1L)).thenReturn(List.of(ownedEvent, remainingEvent));
    when(registrations.findByEventId(11L)).thenReturn(List.of(owned, family, unrelated, cancelled));

    assertTrue(service.handle(new LeaveCommunityCommand(1L, 20L)).isSuccess());

    verify(lifecycle).delete(10L);
    verify(registrations).save(argThat(saved -> saved.id().equals(21L)
        && saved.status() == EventRegistrationStatus.CANCELLED));
    verify(registrations).save(argThat(saved -> saved.id().equals(22L)
        && saved.status() == EventRegistrationStatus.REGISTERED
        && saved.activeParticipantCount() == 2
        && !saved.includes(20L)));
    verify(registrations, never()).save(argThat(saved -> saved.id().equals(23L) || saved.id().equals(24L)));
    verify(events).save(argThat(saved -> saved.getId().equals(11L) && saved.getCapacity() == 5));
    verify(memberships).delete(membership);
  }

  @Test
  void leavingWithoutActiveReservationsOnlyRemovesMembership() {
    CommunityMembership membership = new CommunityMembership(5L, 1L, 20L, CommunityRole.MEMBER);
    Event event = event(11L, 99L, 3);
    EventRegistration cancelled = new EventRegistration(
        24L, 11L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
        EventRegistrationStatus.CANCELLED);
    when(memberships.findByCommunityIdAndUserId(1L, 20L)).thenReturn(Optional.of(membership));
    when(communities.findByIdForUpdate(1L)).thenReturn(Optional.of(community()));
    when(events.findByCommunityIdForUpdate(1L)).thenReturn(List.of(event));
    when(registrations.findByEventId(11L)).thenReturn(List.of(cancelled));

    assertTrue(service.handle(new LeaveCommunityCommand(1L, 20L)).isSuccess());

    verify(registrations, never()).save(any());
    verify(events, never()).save(any());
    verify(memberships).delete(membership);
  }

  private Community community() {
    return new Community(1L, "Green Neighbors", null, CommunityType.LOCAL, null, "Lima", null, null, 20L);
  }

  private Event event(Long id, Long author, int availablePlaces) {
    return new Event(id, 1L, author, "Cleanup", null, LocalDate.of(2027, 6, 12),
        LocalTime.NOON, "Lima", null, null, availablePlaces, null);
  }
}
