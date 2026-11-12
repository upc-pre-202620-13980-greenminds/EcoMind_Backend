package pe.greenminds.ecomind.community.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.community.domain.model.commands.UpdateEventCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

class EventDomainInvariantTests {
  private static final LocalDate EVENT_DATE = LocalDate.of(2027, 5, 4);

  private Event event(int availablePlaces) {
    return new Event(
        7L,
        3L,
        11L,
        "Cleanup",
        "Park cleanup",
        EVENT_DATE,
        LocalTime.of(9, 30),
        "Lima",
        -12.0,
        -77.0,
        availablePlaces,
        "https://images.example/event.png");
  }

  @Test
  void eventRejectsEveryMissingRequiredFieldAndNegativeCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new Event(null, null, 11L, "Cleanup", null, EVENT_DATE,
        LocalTime.NOON, null, null, null, 2, null));
    assertThrows(IllegalArgumentException.class, () -> new Event(null, 3L, null, "Cleanup", null, EVENT_DATE,
        LocalTime.NOON, null, null, null, 2, null));
    assertThrows(IllegalArgumentException.class, () -> new Event(null, 3L, 11L, " ", null, EVENT_DATE,
        LocalTime.NOON, null, null, null, 2, null));
    assertThrows(IllegalArgumentException.class, () -> new Event(null, 3L, 11L, "Cleanup", null, null,
        LocalTime.NOON, null, null, null, 2, null));
    assertThrows(IllegalArgumentException.class, () -> new Event(null, 3L, 11L, "Cleanup", null, EVENT_DATE,
        null, null, null, null, 2, null));
    assertThrows(IllegalArgumentException.class, () -> new Event(null, 3L, 11L, "Cleanup", null, EVENT_DATE,
        LocalTime.NOON, null, null, null, null, null));
    assertThrows(IllegalArgumentException.class, () -> new Event(null, 3L, 11L, "Cleanup", null, EVENT_DATE,
        LocalTime.NOON, null, null, null, -1, null));
    assertEquals("Cleanup", event(2).getName());
  }

  @Test
  void reservingAndReleasingPlacesPreservesDetailsAndEnforcesBounds() {
    Event original = event(3);
    Event reserved = original.reservePlaces(2);
    assertEquals(1, reserved.getCapacity());
    assertEquals(3, original.getCapacity());
    assertEquals(original.getId(), reserved.getId());
    assertEquals(original.getName(), reserved.getName());

    assertEquals(5, reserved.releasePlaces(4).getCapacity());
    assertThrows(IllegalArgumentException.class, () -> original.reservePlaces(0));
    assertThrows(IllegalArgumentException.class, () -> original.reservePlaces(4));
    assertThrows(IllegalArgumentException.class, () -> original.releasePlaces(0));
    assertThrows(ArithmeticException.class, () -> event(Integer.MAX_VALUE).releasePlaces(1));
  }

  @Test
  void updatingDetailsRetainsNullsTrimsValuesAndClearsBlankOptionalText() {
    Event original = event(8);
    Event updated = original.updateDetails(new UpdateEventCommand(
        original.getId(), 11L, "  New cleanup  ", "   ", null, null, "  Miraflores  ",
        -13.0, -76.0, 9, "  https://images.example/new.png  "));

    assertEquals("New cleanup", updated.getName());
    assertNull(updated.getDescription());
    assertEquals(EVENT_DATE, updated.getDate());
    assertEquals(LocalTime.of(9, 30), updated.getStartTime());
    assertEquals("Miraflores", updated.getLocation());
    assertEquals(-13.0, updated.getLatitude());
    assertEquals(-76.0, updated.getLongitude());
    assertEquals(9, updated.getCapacity());
    assertEquals("https://images.example/new.png", updated.getImageUrl());

    Event unchangedOptionalText = original.updateDetails(new UpdateEventCommand(
        original.getId(), 11L, null, null, null, null, null, null, null, null, null));
    assertEquals(original.getDescription(), unchangedOptionalText.getDescription());
    assertEquals(original.getLocation(), unchangedOptionalText.getLocation());
    assertEquals(original.getImageUrl(), unchangedOptionalText.getImageUrl());
    Event clearedOptionalText = original.updateDetails(new UpdateEventCommand(
        original.getId(), 11L, null, "", null, null, " ", null, null, null, "\t"));
    assertNull(clearedOptionalText.getDescription());
    assertNull(clearedOptionalText.getLocation());
    assertNull(clearedOptionalText.getImageUrl());
  }

  @Test
  void coordinatesMustBeAnInRangePair() {
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(-12.0, null));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(null, -77.0));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(-90.1, -77.0));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(90.1, -77.0));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(Double.NaN, -77.0));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(-12.0, -180.1));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(-12.0, 180.1));
    assertThrows(IllegalArgumentException.class, () -> updateCoordinates(-12.0, Double.POSITIVE_INFINITY));
    assertEquals(90.0, updateCoordinates(90.0, 180.0).getLatitude());
    assertEquals(-180.0, updateCoordinates(-90.0, -180.0).getLongitude());
  }

  private Event updateCoordinates(Double latitude, Double longitude) {
    return event(5).updateDetails(new UpdateEventCommand(
        7L, 11L, null, null, null, null, null, latitude, longitude, null, null));
  }

  @Test
  void chatRemainsOpenUntilTwentyFourHoursAfterTheEvent() {
    Event event = event(1);
    Instant expiration = event.getChatExpiresAt();
    assertTrue(event.isChatOpen(expiration.minusNanos(1)));
    assertFalse(event.isChatOpen(expiration));
  }

  @Test
  void eventRegistrationTracksCancellationAndFamilyParticipants() {
    EventRegistration individual = new EventRegistration(
        1L, 7L, 11L, EventRegistrationType.INDIVIDUAL, null, 1,
        EventRegistrationStatus.REGISTERED);
    assertTrue(individual.includes(11L));
    assertFalse(individual.includes(12L));
    assertEquals(1, individual.activeParticipantCount());
    assertEquals(0, individual.cancel().activeParticipantCount());
    assertFalse(individual.cancel().includes(11L));

    EventRegistration family = new EventRegistration(
        2L, 7L, 20L, EventRegistrationType.FAMILY, 90L, 3,
        EventRegistrationStatus.REGISTERED, Set.of(20L, 21L, 22L));
    assertEquals(3, family.activeParticipantCount());
    assertFalse(family.removeParticipant(21L).includes(21L));
    assertTrue(family.removeParticipant(21L).includes(22L));
    EventRegistration lastParticipant = family.removeParticipant(20L).removeParticipant(21L)
        .removeParticipant(22L);
    assertEquals(EventRegistrationStatus.CANCELLED, lastParticipant.status());
    assertEquals(0, lastParticipant.activeParticipantCount());
    assertThrows(IllegalArgumentException.class, () -> family.removeParticipant(99L));
    EventRegistration partialSnapshot = new EventRegistration(
        3L, 7L, 20L, EventRegistrationType.FAMILY, 90L, 3,
        EventRegistrationStatus.REGISTERED, Set.of());
    assertEquals(3, partialSnapshot.activeParticipantCount());
  }

  @Test
  void eventRegistrationRejectsInvalidIdsCountsAndParticipantSnapshots() {
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, null, 11L, EventRegistrationType.INDIVIDUAL, null, 1,
        EventRegistrationStatus.REGISTERED));
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, 7L, 11L, EventRegistrationType.INDIVIDUAL, null, 0,
        EventRegistrationStatus.REGISTERED));
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, 7L, 11L, null, null, 1, EventRegistrationStatus.REGISTERED));
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, 7L, 11L, EventRegistrationType.INDIVIDUAL, null, 1, null));
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, 7L, 11L, EventRegistrationType.FAMILY, 90L, 1,
        EventRegistrationStatus.REGISTERED, Set.of(0L)));
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, 7L, 11L, EventRegistrationType.FAMILY, 90L, 1,
        EventRegistrationStatus.REGISTERED, Set.of(11L, 12L)));
    assertThrows(IllegalArgumentException.class, () -> new EventRegistration(
        null, 7L, 11L, EventRegistrationType.FAMILY, 90L, 1,
        EventRegistrationStatus.CANCELLED, Set.of(11L)));
  }

  @Test
  void eventChatMessageValidatesAndNormalizesText() {
    EventChatMessage message = new EventChatMessage(
        1L, 7L, 11L, "Ana", "  Hello  ", Instant.parse("2027-05-04T15:00:00Z"), UUID.randomUUID());
    assertEquals("Hello", message.text());
    assertThrows(NullPointerException.class, () -> new EventChatMessage(
        null, null, 11L, "Ana", "hello", Instant.now(), UUID.randomUUID()));
    assertThrows(IllegalArgumentException.class, () -> new EventChatMessage(
        null, 0L, 11L, "Ana", "hello", Instant.now(), UUID.randomUUID()));
    assertThrows(IllegalArgumentException.class, () -> new EventChatMessage(
        null, 7L, 0L, "Ana", "hello", Instant.now(), UUID.randomUUID()));
    assertThrows(IllegalArgumentException.class, () -> new EventChatMessage(
        null, 7L, 11L, "Ana", "  ", Instant.now(), UUID.randomUUID()));
    assertThrows(IllegalArgumentException.class, () -> new EventChatMessage(
        null, 7L, 11L, "Ana", "x".repeat(2001), Instant.now(), UUID.randomUUID()));
    assertEquals(2000, new EventChatMessage(
        null, 7L, 11L, "Ana", "x".repeat(2000), Instant.now(), UUID.randomUUID()).text().length());
  }
}
