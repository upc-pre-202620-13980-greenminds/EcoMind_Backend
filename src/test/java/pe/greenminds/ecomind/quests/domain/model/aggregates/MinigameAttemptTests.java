package pe.greenminds.ecomind.quests.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MinigameAttemptTests {

    @Test
    void newAttemptHasNoResultYet() {
        var attempt = new MinigameAttempt(7L, 11L, 3L);

        assertEquals(MinigameAttemptStatus.STARTED, attempt.getStatus());
        assertNull(attempt.getSuccessful());
    }

    @Test
    void completedAttemptStoresSuccessfulResult() {
        var attempt = new MinigameAttempt(7L, 11L, 3L);

        attempt.finish(80, Map.of("level", 2), true);

        assertEquals(MinigameAttemptStatus.COMPLETED, attempt.getStatus());
        assertTrue(attempt.getSuccessful());
        assertEquals(80, attempt.getScore());
        assertNotNull(attempt.getEndDate());
    }

    @Test
    void completedAttemptStoresFailedResult() {
        var attempt = new MinigameAttempt(7L, 11L, 3L);

        attempt.finish(40, Map.of(), false);

        assertEquals(MinigameAttemptStatus.COMPLETED, attempt.getStatus());
        assertFalse(attempt.getSuccessful());
    }

    @Test
    void cancelledAttemptHasNoSuccessResult() {
        var attempt = new MinigameAttempt(7L, 11L, 3L);

        attempt.cancel();

        assertEquals(MinigameAttemptStatus.CANCELLED, attempt.getStatus());
        assertNull(attempt.getSuccessful());
    }

    @Test
    void completedAttemptCannotBeFinishedAgain() {
        var attempt = new MinigameAttempt(7L, 11L, 3L);
        attempt.finish(80, Map.of(), true);

        assertThrows(IllegalStateException.class,
                () -> attempt.finish(90, Map.of(), true));
    }
}
