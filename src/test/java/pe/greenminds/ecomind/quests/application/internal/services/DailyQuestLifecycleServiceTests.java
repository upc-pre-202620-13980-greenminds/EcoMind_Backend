package pe.greenminds.ecomind.quests.application.internal.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class DailyQuestLifecycleServiceTests {
  @Mock QuestRepository quests;
  @Mock QuestUserRepository users;
  @Mock ActivityRepository activities;
  @Mock ActivityUserRepository assignments;
  DailyQuestLifecycleService service;
  final LocalDate today = LocalDate.of(2026, 10, 10);

  @BeforeEach
  void setup() {
    service = spy(new DailyQuestLifecycleService(quests, users, activities, assignments));
    lenient().doReturn(today).when(service).today();
  }

  Quest quest(long id, LocalDate date) {
    return new Quest(
        id,
        null,
        "Daily action - " + date,
        Category.ENERGY,
        "Save",
        QuestType.DAILY_QUEST,
        8,
        new Reward(1, 5),
        2,
        null,
        Theme.CHECKBOX,
        date,
        id,
        1,
        QuestPublicationStatus.PUBLISHED);
  }

  Activity activity(long quest) {
    return new Activity(4L, quest, "Check", 1, ActivityType.CHECKBOX, Map.of(), null);
  }

  void existing() {
    when(quests.findByTypeAndAssignedDate(QuestType.DAILY_QUEST, today))
        .thenReturn(Optional.of(quest(3L, today)));
    when(activities.findByQuestsIdOrderByOrderAsc(3L)).thenReturn(List.of(activity(3L)));
  }

  @Test
  void emptyCatalogDoesNotInventDailyQuest() {
    assertTrue(service.ensureTodayDailyQuest().isEmpty());
    verify(quests, never()).save(any());
  }

  @Test
  void existingDailyQuestIsReused() {
    existing();
    assertEquals(3L, service.ensureTodayDailyQuest().orElseThrow().getId());
    verify(quests, never()).save(any());
  }

  @Test
  void latestPublishedTemplateIsClonedWithActivities() {
    var old = quest(2L, today.minusDays(2));
    var recent = quest(3L, today.minusDays(1));
    when(quests.findByPublicationStatus(QuestPublicationStatus.PUBLISHED))
        .thenReturn(List.of(old, recent));
    when(activities.findByQuestsIdOrderByOrderAsc(2L)).thenReturn(List.of(activity(2L)));
    when(activities.findByQuestsIdOrderByOrderAsc(3L)).thenReturn(List.of(activity(3L)));
    when(quests.save(any()))
        .thenAnswer(
            i -> {
              Quest q = i.getArgument(0);
              q.setId(5L);
              return q;
            });
    var created = service.ensureTodayDailyQuest().orElseThrow();
    assertEquals(today, created.getAssignedDate());
    assertEquals(QuestPublicationStatus.PUBLISHED, created.getPublicationStatus());
    assertEquals("Daily action - 2026-10-10", created.getTitle());
    assertEquals(5L, created.getVersionGroupId());
    verify(activities)
        .save(argThat(a -> a.getQuestId().equals(5L) && a.getDescription().equals("Check")));
  }

  @Test
  void emptyTodaysQuestRecoversActivitiesFromPreviousTemplate() {
    when(quests.findByTypeAndAssignedDate(QuestType.DAILY_QUEST, today))
        .thenReturn(Optional.of(quest(3L, today)));
    when(activities.findByQuestsIdOrderByOrderAsc(3L)).thenReturn(List.of());
    when(quests.findByPublicationStatus(QuestPublicationStatus.PUBLISHED))
        .thenReturn(List.of(quest(2L, today.minusDays(1)), quest(3L, today)));
    when(activities.findByQuestsIdOrderByOrderAsc(2L)).thenReturn(List.of(activity(2L)));
    service.ensureTodayDailyQuest();
    verify(activities).save(argThat(a -> a.getQuestId().equals(3L)));
  }

  @Test
  void assignmentWithoutTemplateReturnsEmpty() {
    assertTrue(service.ensureTodayDailyQuestForUser(20L).isEmpty());
    verify(users, never()).save(any());
  }

  @Test
  void existingAssignmentIsNotDuplicated() {
    existing();
    var prior = new QuestUser(20L, 3L, null);
    when(users.findFirstByUserIdAndQuestId(20L, 3L)).thenReturn(Optional.of(prior));
    assertSame(prior, service.ensureTodayDailyQuestForUser(20L).orElseThrow());
    verifyNoInteractions(assignments);
  }

  @Test
  void newDailyAssignmentContainsEveryTemplateActivity() {
    existing();
    when(users.save(any()))
        .thenAnswer(
            i -> {
              QuestUser q = i.getArgument(0);
              q.setId(6L);
              return q;
            });
    assertEquals(6L, service.ensureTodayDailyQuestForUser(20L).orElseThrow().getId());
    verify(assignments)
        .save(argThat(a -> a.getQuestUserId().equals(6L) && a.getActivityId().equals(4L)));
  }

  @Test
  void concurrentAssignmentReturnsWinnerInsteadOfDuplicating() {
    existing();
    var winner = new QuestUser(20L, 3L, null);
    when(users.findFirstByUserIdAndQuestId(20L, 3L))
        .thenReturn(Optional.empty(), Optional.of(winner));
    when(users.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
    assertSame(winner, service.ensureTodayDailyQuestForUser(20L).orElseThrow());
    verifyNoInteractions(assignments);
  }

  @Test
  void expirationPreservesProgressForReview() {
    var q = new QuestUser(20L, 3L, null);
    q.updateProgress(50.0);
    when(users.findDailyQuestUsersBeforeDateAndStatusIn(
            eq(QuestType.DAILY_QUEST), eq(today), anyList()))
        .thenReturn(List.of(q));
    service.expireOpenDailyQuests();
    assertEquals(QuestStatus.EXPIRED, q.getStatus());
    assertEquals(50.0, q.getProgress());
    verify(users).save(q);
  }

  @Test
  void userExpirationIsScopedToRequestedUser() {
    var q = new QuestUser(20L, 3L, null);
    when(users.findDailyQuestUsersByUserIdBeforeDateAndStatusIn(
            eq(20L), eq(QuestType.DAILY_QUEST), eq(today), anyList()))
        .thenReturn(List.of(q));
    service.expireOpenDailyQuestsForUser(20L);
    assertEquals(QuestStatus.EXPIRED, q.getStatus());
    verify(users).save(q);
  }
}
