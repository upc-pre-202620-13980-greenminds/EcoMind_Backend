package pe.greenminds.ecomind.community.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.*;
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
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventParticipationQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventParticipationQueryServiceImplTests {
  @Mock EventRepository events;
  @Mock EventRegistrationRepository registrations;
  @Mock CommunityRepository communities;
  @Mock CommunityActorGateway actors;
  EventParticipationQueryServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EventParticipationQueryServiceImpl(events, registrations, communities, actors);
  }

  private Event event(long id, long communityId, long creatorId, int capacity) {
    return new Event(id, communityId, creatorId, "Cleanup " + id, null,
        LocalDate.of(2027, 6, 12), LocalTime.NOON, "Lima", null, null, capacity, null);
  }

  @Test
  void communityIsRequiredWhenListingEveryone() {
    assertTrue(service.handle(new SearchEventParticipationQuery(null, 20L, false)).isFailure());
    verifyNoInteractions(events, registrations, communities, actors);
  }

  @Test
  void mineIncludesJoinedMembersAndFamilyOwnersWhoCanCancel() {
    Event owned = event(1, 8, 20, 3);
    Event familyParticipant = event(2, 8, 30, 2);
    Event familyOwner = event(3, 9, 30, 1);
    Event unjoined = event(4, 8, 30, 4);
    Event cancelledOnly = event(5, 8, 30, 4);

    when(actors.getRegistrableFamilyId(20L)).thenReturn(77L);
    when(actors.getUserName(20L)).thenReturn("Ana");
    when(actors.getUserName(30L)).thenReturn("Bruno");
    when(events.findAll(null)).thenReturn(List.of(owned, familyParticipant, familyOwner, unjoined, cancelledOnly));
    when(registrations.findByEventId(1L)).thenReturn(List.of(
        new EventRegistration(101L, 1L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.REGISTERED),
        new EventRegistration(102L, 1L, 40L, EventRegistrationType.FAMILY, 91L, 3,
            EventRegistrationStatus.REGISTERED, Set.of(20L, 40L, 41L)),
        new EventRegistration(103L, 1L, 50L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.CANCELLED)));
    when(registrations.findByEventId(2L)).thenReturn(List.of(
        new EventRegistration(201L, 2L, 30L, EventRegistrationType.FAMILY, 92L, 2,
            EventRegistrationStatus.REGISTERED, Set.of(20L, 30L))));
    when(registrations.findByEventId(3L)).thenReturn(List.of(
        new EventRegistration(301L, 3L, 20L, EventRegistrationType.FAMILY, 93L, 2,
            EventRegistrationStatus.REGISTERED, Set.of(21L, 22L))));
    when(registrations.findByEventId(4L)).thenReturn(List.of());
    when(registrations.findByEventId(5L)).thenReturn(List.of(
        new EventRegistration(501L, 5L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.CANCELLED)));
    when(communities.findById(8L)).thenReturn(Optional.of(community(8L, "Lima Verde")));
    when(communities.findById(9L)).thenReturn(Optional.empty());

    var result = service.handle(new SearchEventParticipationQuery(null, 20L, true)).toOptional().orElseThrow();

    assertEquals(3, result.size());
    assertTrue(result.get(0).joined());
    assertTrue(result.get(0).creator());
    assertFalse(result.get(0).canCancelGroup());
    assertEquals(101L, result.get(0).registrationId());
    assertEquals(4, result.get(0).participantCount());
    assertEquals(7, result.get(0).totalCapacity());
    assertEquals("Lima Verde", result.get(0).communityName());
    assertEquals("Ana", result.get(0).creatorName());
    assertTrue(result.get(0).chatAvailable());
    assertEquals(77L, result.get(0).familyId());

    assertTrue(result.get(1).joined());
    assertFalse(result.get(1).creator());
    assertFalse(result.get(1).canCancelGroup());
    assertEquals(201L, result.get(1).registrationId());

    assertFalse(result.get(2).joined());
    assertTrue(result.get(2).canCancelGroup());
    assertEquals(301L, result.get(2).registrationId());
    assertEquals("", result.get(2).communityName());
    verify(events).findAll(null);
  }

  @Test
  void allCommunityEventsIncludeUnjoinedAndOmitCancelledRegistrations() {
    Event event = event(6, 12, 30, 4);
    when(actors.getRegistrableFamilyId(20L)).thenReturn(null);
    when(actors.getUserName(30L)).thenReturn("Bruno");
    when(events.findAll(12L)).thenReturn(List.of(event));
    when(registrations.findByEventId(6L)).thenReturn(List.of(
        new EventRegistration(601L, 6L, 20L, EventRegistrationType.INDIVIDUAL, null, 1,
            EventRegistrationStatus.CANCELLED)));
    when(communities.findById(12L)).thenReturn(Optional.of(community(12L, "Gardeners")));

    var result = service.handle(new SearchEventParticipationQuery(12L, 20L, false))
        .toOptional().orElseThrow();

    assertEquals(1, result.size());
    assertFalse(result.getFirst().joined());
    assertEquals(0, result.getFirst().participantCount());
    assertEquals(4, result.getFirst().totalCapacity());
    assertNull(result.getFirst().registrationId());
    assertFalse(result.getFirst().chatAvailable());
    assertNull(result.getFirst().familyId());
    verify(events).findAll(12L);
  }

  private Community community(Long id, String name) {
    return new Community(id, name, null, CommunityType.LOCAL, null, "Lima", null, null, 30L);
  }
}
