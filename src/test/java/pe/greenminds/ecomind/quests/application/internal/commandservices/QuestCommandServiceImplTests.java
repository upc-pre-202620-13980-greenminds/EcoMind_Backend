package pe.greenminds.ecomind.quests.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class QuestCommandServiceImplTests {
  @Mock QuestRepository quests;
  @Mock ActivityRepository activities;
  @InjectMocks QuestCommandServiceImpl service;

  Quest quest() {
    var q =
        new Quest(
            3L,
            null,
            "Lights",
            Category.ENERGY,
            "Save energy",
            QuestType.ACTIVITIES,
            8,
            new Reward(2, 10),
            2,
            null,
            Theme.CHECKBOX,
            null);
    q.initializeVersionGroup(3L);
    when(quests.findById(3L)).thenReturn(Optional.of(q));
    return q;
  }

  UpdateQuestCommand update(String title) {
    return new UpdateQuestCommand(
        3L,
        null,
        title,
        "Save energy",
        Category.ENERGY,
        QuestType.ACTIVITIES,
        2,
        20,
        8,
        2,
        Theme.CHECKBOX,
        null,
        null);
  }

  @Test
  void unknownQuestCannotBeUpdated() {
    assertTrue(service.handle(update("New")).isFailure());
    verifyNoInteractions(activities);
  }

  @Test
  void draftIsUpdatedInPlace() {
    var q = quest();
    when(quests.save(any())).thenAnswer(i -> i.getArgument(0));
    var result = service.handle(update("New")).toOptional().orElseThrow();
    assertSame(q, result);
    assertEquals("New", result.getTitle());
    assertEquals(20, result.getReward().ecopoints());
    verifyNoInteractions(activities);
  }

  @Test
  void publishedQuestCreatesNewDraftAndPreservesOriginalActivities() {
    var q = quest();
    q.publish();
    var a =
        new Activity(
            7L, 3L, "Check room", 1, ActivityType.CHECKBOX, Map.of("required", true), "room");
    when(activities.findByQuestsIdOrderByOrderAsc(3L)).thenReturn(List.of(a));
    when(quests.save(any()))
        .thenAnswer(
            i -> {
              Quest x = i.getArgument(0);
              if (x.getId() == null) x.setId(4L);
              return x;
            });
    var draft = service.handle(update("New version")).toOptional().orElseThrow();
    assertEquals(QuestPublicationStatus.ARCHIVED, q.getPublicationStatus());
    assertEquals("Lights", q.getTitle());
    assertEquals(QuestPublicationStatus.DRAFT, draft.getPublicationStatus());
    assertEquals(2, draft.getVersionNumber());
    assertEquals(3L, draft.getVersionGroupId());
    verify(activities)
        .save(
            argThat(
                copy ->
                    copy.getQuestId().equals(4L)
                        && copy.getDescription().equals(a.getDescription())
                        && copy.getActivityConfiguration().equals(a.getActivityConfiguration())));
    assertEquals(3L, a.getQuestId());
  }

  @Test
  void invalidUpdateIsRejected() {
    quest();
    assertTrue(service.handle(update(null)).isFailure());
    verify(quests, never()).save(any());
  }

  @Test
  void archivedVersionCannotBeEdited() {
    var q = quest();
    q.publish();
    q.archive();
    assertTrue(service.handle(update("New")).isFailure());
    verify(quests, never()).save(any());
  }

  @Test
  void missingQuestCannotBePublished() {
    assertTrue(service.handle(new PublishQuestCommand(3L)).isFailure());
  }

  @Test
  void onlyOneVersionCanBePublished() {
    var q = quest();
    when(quests.findPublishedByVersionGroupId(3L)).thenReturn(Optional.of(q));
    assertTrue(service.handle(new PublishQuestCommand(3L)).isFailure());
    verify(quests, never()).save(any());
  }

  @Test
  void archivedQuestCannotBePublished() {
    var q = quest();
    q.publish();
    q.archive();
    assertTrue(service.handle(new PublishQuestCommand(3L)).isFailure());
  }

  @Test
  void missingQuestCannotBeArchived() {
    assertTrue(service.handle(new ArchiveQuestCommand(3L)).isFailure());
  }
}
