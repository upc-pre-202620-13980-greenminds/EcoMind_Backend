package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

class IntegrationContractTests {
    @Test
    void multiplierValidityHasExclusiveEndAndRejectsInvalidFactors() {
        Instant start = Instant.parse("2026-10-07T00:00:00Z");
        Instant end = start.plusSeconds(3600);
        var multiplier =
                new MonetizationContextFacade.ActiveXpMultiplier(
                        UUID.randomUUID(), new BigDecimal("1.5"), start, end);
        assertTrue(multiplier.isActiveAt(start));
        assertFalse(multiplier.isActiveAt(end));
        assertFalse(multiplier.isActiveAt(start.minusSeconds(1)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MonetizationContextFacade.ActiveXpMultiplier(
                                UUID.randomUUID(), BigDecimal.ZERO, start, end));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MonetizationContextFacade.CreditRewardGems(UUID.randomUUID(), 1L, 0));
    }

    @Test
    void publicationConfirmationRequiresOriginalRequestAndPersistedPublication() {
        UUID requestId = UUID.randomUUID();
        UUID awardId = UUID.randomUUID();
        Long communityId = 73L;
        Long publicationId = 73L;
        var event =
                new PublicationCreatedIntegrationEvent(
                        UUID.randomUUID(),
                        requestId,
                        awardId,
                        1L,
                        communityId,
                        publicationId,
                        Instant.now());
        assertEquals(requestId, event.requestId());
        assertEquals(publicationId, event.publicationId());
        assertThrows(
                NullPointerException.class,
                () ->
                        new PublicationCreatedIntegrationEvent(
                                UUID.randomUUID(),
                                requestId,
                                awardId,
                                1L,
                                communityId,
                                null,
                                Instant.now()));
    }
}
