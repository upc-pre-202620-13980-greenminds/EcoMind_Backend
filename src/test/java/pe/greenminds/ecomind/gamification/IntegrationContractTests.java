package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.quests.interfaces.acl.events.CollaborativeQuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.resources.QuestRewardResource;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;

class IntegrationContractTests {
  @Test
  void collaborativeParticipantsAreUniqueAndImmutable() {
    var participants = new ArrayList<>(List.of(1L, 2L));
    var event = collaborative(participants);
    participants.clear();
    assertEquals(List.of(1L, 2L), event.participantIds());
    assertThrows(UnsupportedOperationException.class, () -> event.participantIds().add(3L));
    assertThrows(IllegalArgumentException.class, () -> collaborative(List.of(1L, 1L)));
    assertThrows(IllegalArgumentException.class, () -> collaborative(List.of(-1L)));
    assertThrows(IllegalArgumentException.class, () -> new QuestRewardResource(-1, 0, 0));
  }

  @Test
  void multiplierValidityHasExclusiveEndAndRejectsInvalidFactors() {
    Instant start = Instant.parse("2026-10-07T00:00:00Z");
    Instant end = start.plusSeconds(3600);
    var multiplier = new MonetizationContextFacade.ActiveXpMultiplier(UUID.randomUUID(), new BigDecimal("1.5"), start, end);
    assertTrue(multiplier.isActiveAt(start));
    assertFalse(multiplier.isActiveAt(end));
    assertFalse(multiplier.isActiveAt(start.minusSeconds(1)));
    assertThrows(IllegalArgumentException.class, () -> new MonetizationContextFacade.ActiveXpMultiplier(UUID.randomUUID(), BigDecimal.ZERO, start, end));
    assertThrows(IllegalArgumentException.class, () -> new MonetizationContextFacade.CreditRewardGems(UUID.randomUUID(), 1L, 0));
  }

  @Test
  void publicationConfirmationRequiresOriginalRequestAndPersistedPublication() {
    UUID requestId = UUID.randomUUID();
    UUID awardId = UUID.randomUUID();
    UUID communityId = UUID.randomUUID();
    UUID publicationId = UUID.randomUUID();
    var event = new PublicationCreatedIntegrationEvent(UUID.randomUUID(), requestId, awardId, 1L,
        communityId, publicationId, Instant.now());
    assertEquals(requestId, event.requestId());
    assertEquals(publicationId, event.publicationId());
    assertThrows(NullPointerException.class, () -> new PublicationCreatedIntegrationEvent(UUID.randomUUID(), requestId,
        awardId, 1L, communityId, null, Instant.now()));
  }

  private CollaborativeQuestCompletedIntegrationEvent collaborative(List<Long> participants) {
    return new CollaborativeQuestCompletedIntegrationEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        participants, new QuestRewardResource(10, 5, 0), Instant.now());
  }
}
