package pe.greenminds.ecomind.quests.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import static org.junit.jupiter.api.Assertions.*;

class QuestTests {

    @Test
    void newQuestStartsAsFirstDraft() {
        var quest = draft();

        assertEquals(QuestPublicationStatus.DRAFT, quest.getPublicationStatus());
        assertEquals(1, quest.getVersionNumber());
        assertFalse(quest.acceptsNewAssignments());
    }

    @Test
    void publishingDraftAllowsNewAssignments() {
        var quest = draft();
        quest.initializeVersionGroup(10L);

        quest.publish();

        assertEquals(QuestPublicationStatus.PUBLISHED, quest.getPublicationStatus());
        assertTrue(quest.acceptsNewAssignments());
    }

    @Test
    void publishedQuestCannotBeEditedDirectly() {
        var quest = draft();
        quest.initializeVersionGroup(10L);
        quest.publish();

        assertThrows(IllegalStateException.class, () -> quest.update(
                null, "Changed", Category.ENERGY, "Changed", QuestType.ACTIVITIES,
                10, new Reward(5, 20), 15, null, Theme.CHECKBOX, null));
    }

    @Test
    void publishedQuestCreatesNextDraftInSameVersionGroup() {
        var quest = draft();
        quest.initializeVersionGroup(10L);
        quest.publish();

        var next = quest.createNextDraft(
                null, "Version 2", Category.ENERGY, "Updated", QuestType.ACTIVITIES,
                10, new Reward(6, 25), 12, null, Theme.CHECKBOX, null);

        assertEquals(10L, next.getVersionGroupId());
        assertEquals(2, next.getVersionNumber());
        assertEquals(QuestPublicationStatus.DRAFT, next.getPublicationStatus());
        assertEquals(QuestPublicationStatus.PUBLISHED, quest.getPublicationStatus());
    }

    @Test
    void archivedQuestCannotBePublishedOrEdited() {
        var quest = draft();
        quest.archive();

        assertThrows(IllegalStateException.class, quest::publish);
        assertThrows(IllegalStateException.class, () -> quest.update(
                null, "Changed", Category.ENERGY, "Changed", QuestType.ACTIVITIES,
                10, new Reward(5, 20), 15, null, Theme.CHECKBOX, null));
    }

    private Quest draft() {
        return new Quest(null, "Save energy", Category.ENERGY, "Turn off lights",
                QuestType.ACTIVITIES, 10, new Reward(5, 20), 10, null,
                Theme.CHECKBOX, null);
    }
}
