package pe.greenminds.ecomind.quests.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;

import static org.junit.jupiter.api.Assertions.*;

class QuestUserTests {

    @Test
    void cancellingPreservesAssignmentAndMarksItCancelled() {
        var assignment = new QuestUser(20L, 30L, null);

        assignment.cancel();

        assertEquals(QuestStatus.CANCELLED, assignment.getStatus());
        assertNotNull(assignment.getEndDate());
        assertEquals(20L, assignment.getUserId());
        assertEquals(30L, assignment.getQuestId());
    }

    @Test
    void completedAssignmentCannotBeCancelled() {
        var assignment = new QuestUser(
                1L, 20L, 30L, QuestStatus.COMPLETED, 100.0,
                java.time.LocalDate.now(), null);

        assertThrows(IllegalStateException.class, assignment::cancel);
    }

    @Test
    void cancelledAssignmentCannotChangeProgress() {
        var assignment = new QuestUser(20L, 30L, null);
        assignment.cancel();

        assertThrows(IllegalStateException.class, () -> assignment.updateProgress(50.0));
    }
}
