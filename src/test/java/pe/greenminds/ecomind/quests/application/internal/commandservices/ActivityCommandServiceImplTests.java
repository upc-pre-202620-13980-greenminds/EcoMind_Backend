package pe.greenminds.ecomind.quests.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class ActivityCommandServiceImplTests {
  @Mock ActivityRepository activities;
  @Mock QuestRepository quests;
  @Mock ActivityUserRepository assignments;
  ActivityCommandServiceImpl service;

  @BeforeEach
  void setup() {
    service = new ActivityCommandServiceImpl(activities, quests, assignments);
  }

  void quest(boolean published) {
    var q =
        new Quest(
            1L,
            null,
            "Save energy",
            Category.ENERGY,
            "Switch off lights",
            QuestType.ACTIVITIES,
            8,
            new Reward(2, 10),
            2,
            null,
            Theme.CHECKBOX,
            null);
    if (published) q.publish();
    when(quests.findById(1L)).thenReturn(Optional.of(q));
  }

  Activity activity(long id, int order) {
    var a = new Activity(1L, "Check room", order, ActivityType.CHECKBOX, Map.of(), null);
    a.setId(id);
    return a;
  }

  @Test
  void missingQuestCannotReceiveActivities() {
    assertTrue(
        service
            .handle(new CreateActivityCommand(1L, "Room", 1, ActivityType.CHECKBOX, Map.of(), null))
            .isFailure());
    verifyNoInteractions(activities);
  }

  @Test
  void publishedQuestCannotReceiveActivities() {
    quest(true);
    assertTrue(
        service
            .handle(new CreateActivityCommand(1L, "Room", 1, ActivityType.CHECKBOX, Map.of(), null))
            .isFailure());
    verifyNoInteractions(activities);
  }

  @Test
  void insertingAtBeginningMovesFollowingActivities() {
    quest(false);
    var first = activity(2, 1);
    var second = activity(3, 2);
    when(activities.findByQuestsIdOrderByOrderAsc(1L)).thenReturn(List.of(first, second));
    when(activities.save(any())).thenAnswer(c -> c.getArgument(0));
    var result =
        service
            .handle(
                new CreateActivityCommand(
                    1L, "New first room", 1, ActivityType.CHECKBOX, Map.of(), null))
            .toOptional()
            .orElseThrow();
    assertEquals(1, result.getOrder());
    assertEquals(2, first.getOrder());
    assertEquals(3, second.getOrder());
  }

  @Test
  void orderBeyondEndAppendsActivity() {
    quest(false);
    when(activities.findByQuestsIdOrderByOrderAsc(1L)).thenReturn(List.of(activity(2, 1)));
    when(activities.save(any())).thenAnswer(c -> c.getArgument(0));
    assertEquals(
        2,
        service
            .handle(
                new CreateActivityCommand(
                    1L, "Next room", 20, ActivityType.CHECKBOX, Map.of(), null))
            .toOptional()
            .orElseThrow()
            .getOrder());
  }

  @Test
  void deletionRemovesAssignmentsAndClosesOrderingGap() {
    quest(false);
    var deleted = activity(2, 1);
    var second = activity(3, 2);
    when(activities.findById(2L)).thenReturn(Optional.of(deleted));
    when(activities.findByQuestsIdOrderByOrderAsc(1L)).thenReturn(List.of(second));
    assertTrue(service.handle(new DeleteActivityCommand(2L)).isSuccess());
    verify(assignments).deleteByActivityId(2L);
    verify(activities).deleteById(2L);
    assertEquals(1, second.getOrder());
  }

  @Test
  void publishedActivityCannotBeDeleted() {
    quest(true);
    when(activities.findById(2L)).thenReturn(Optional.of(activity(2, 1)));
    assertTrue(service.handle(new DeleteActivityCommand(2L)).isFailure());
    verifyNoInteractions(assignments);
    verify(activities, never()).deleteById(anyLong());
  }

  @Test
  void activityTypeCannotBeChanged() {
    quest(false);
    when(activities.findById(2L)).thenReturn(Optional.of(activity(2, 1)));
    assertTrue(
        service
            .handle(new UpdateActivityCommand(2L, "Room", 1, ActivityType.WRITE, Map.of(), null))
            .isFailure());
    verify(activities, never()).save(any());
  }

  @Test
  void movingActivityMaintainsContiguousOrdering() {
    quest(false);
    var first = activity(2, 1);
    var second = activity(3, 2);
    when(activities.findById(2L)).thenReturn(Optional.of(first));
    when(activities.findByQuestsIdOrderByOrderAsc(1L)).thenReturn(List.of(first, second));
    when(activities.save(any())).thenAnswer(c -> c.getArgument(0));
    assertTrue(
        service
            .handle(
                new UpdateActivityCommand(
                    2L, "Move room", 2, ActivityType.CHECKBOX, Map.of(), null))
            .isSuccess());
    assertEquals(2, first.getOrder());
    assertEquals(1, second.getOrder());
  }
}
