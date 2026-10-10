package pe.greenminds.ecomind.quests.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.quests.application.internal.services.DailyQuestLifecycleService;
import pe.greenminds.ecomind.quests.application.internal.submission.*;
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class ActivityUserCommandServiceImplTests {
  @Mock ActivityUserRepository assignments;
  @Mock ActivityRepository activities;
  @Mock QuestUserRepository users;
  @Mock CollabQuestSessionRepository sessions;
  @Mock CollabQuestMemberRepository members;
  @Mock ActivitySubmissionHandlerRegistry registry;
  @Mock DailyQuestLifecycleService daily;
  @InjectMocks ActivityUserCommandServiceImpl service;

  Activity activity() {
    var a = new Activity(4L, 3L, "Check", 1, ActivityType.CHECKBOX, Map.of(), null);
    when(activities.findById(4L)).thenReturn(Optional.of(a));
    return a;
  }

  QuestUser user(Long session) {
    var q = new QuestUser(6L, 20L, 3L, session);
    when(users.findById(6L)).thenReturn(Optional.of(q));
    return q;
  }

  ActivityUser assignment(Long session) {
    var a = new ActivityUser(6L, 4L, "Check", Map.of(), session);
    a.setId(5L);
    when(assignments.findById(5L)).thenReturn(Optional.of(a));
    return a;
  }

  SubmitActivityUserCommand submit() {
    return new SubmitActivityUserCommand(5L, Map.of("checked", true));
  }

  void handler() {
    when(registry.findByType(ActivityType.CHECKBOX))
        .thenReturn(Optional.of(new CheckboxActivitySubmissionHandler()));
  }

  @Test
  void missingAssignmentCannotBeSubmitted() {
    assertTrue(service.handle(submit()).isFailure());
    verifyNoInteractions(users);
  }

  @Test
  void missingQuestAssignmentCannotBeSubmitted() {
    assignment(null);
    assertTrue(service.handle(submit()).isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = QuestStatus.class,
      names = {"COMPLETED", "CANCELLED"})
  void closedAssignmentsRejectProgress(QuestStatus status) {
    assignment(null);
    var q = new QuestUser(6L, 20L, 3L, status, 100.0, null, null);
    when(users.findById(6L)).thenReturn(Optional.of(q));
    assertTrue(service.handle(submit()).isFailure());
    verify(assignments, never()).save(any());
  }

  @Test
  void expiredAndRemovedDailyAssignmentIsRechecked() {
    assignment(null);
    var q = new QuestUser(6L, 20L, 3L, null);
    when(users.findById(6L)).thenReturn(Optional.of(q), Optional.empty());
    assertTrue(service.handle(submit()).isFailure());
    verifyNoInteractions(registry);
  }

  @Test
  void missingActivityRejectsSubmission() {
    assignment(null);
    user(null);
    assertTrue(service.handle(submit()).isFailure());
  }

  @Test
  void unsupportedActivityRejectsSubmission() {
    assignment(null);
    user(null);
    activity();
    assertTrue(service.handle(submit()).isFailure());
  }

  @Test
  void invalidCheckboxDoesNotSaveProgress() {
    assignment(null);
    user(null);
    activity();
    handler();
    assertTrue(
        service.handle(new SubmitActivityUserCommand(5L, Map.of("checked", "yes"))).isFailure());
    verify(assignments, never()).save(any());
  }

  @Test
  void validCheckboxUpdatesAssignmentAndAverage() {
    var a = assignment(null);
    var q = user(null);
    activity();
    handler();
    when(assignments.save(any())).thenAnswer(i -> i.getArgument(0));
    when(assignments.findByQuestUserId(6L))
        .thenReturn(List.of(a, new ActivityUser(6L, 7L, "Other", Map.of(), null)));
    assertTrue(service.handle(submit()).isSuccess());
    assertEquals(50.0, q.getProgress());
    assertEquals(QuestStatus.IN_PROGRESS, q.getStatus());
  }

  @Test
  void missingCollaborativeSessionRejectsSubmission() {
    assignment(9L);
    user(9L);
    activity();
    handler();
    assertTrue(service.handle(submit()).isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabQuestStatus.class,
      names = {"PENDING", "COMPLETED", "CANCELLED"})
  void collaborativeProgressRequiresStartedSession(CollabQuestStatus status) {
    assignment(9L);
    user(9L);
    activity();
    handler();
    when(sessions.findById(9L))
        .thenReturn(Optional.of(new CollabQuestSession(9L, 3L, 20L, status, null, null)));
    assertTrue(service.handle(submit()).isFailure());
  }

  @Test
  void unacceptedParticipantCannotSubmit() {
    assignment(9L);
    user(9L);
    activity();
    handler();
    when(sessions.findById(9L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(9L, 3L, 20L, CollabQuestStatus.STARTED, null, null)));
    assertTrue(service.handle(submit()).isFailure());
    verify(assignments, never()).save(any());
  }

  @Test
  void missingPeerProgressDoesNotPartiallyUpdateGroup() {
    var a = assignment(9L);
    user(9L);
    activity();
    handler();
    when(sessions.findById(9L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(9L, 3L, 20L, CollabQuestStatus.STARTED, null, null)));
    when(members.findBySessionIdAndStatusIn(eq(9L), anyList()))
        .thenReturn(
            List.of(
                new CollabQuestMember(
                    9L, 20L, 20L, MemberRole.OWNER, CollabMemberStatus.ACCEPTED)));
    assertTrue(service.handle(submit()).isFailure());
    assertEquals(0.0, a.getProgress());
    verify(assignments, never()).save(any());
  }

  @Test
  void createRequiresExistingQuestAssignment() {
    assertTrue(service.handle(new CreateActivityUserCommand(6L, 4L, null)).isFailure());
  }

  @Test
  void createRequiresExistingActivity() {
    user(null);
    assertTrue(service.handle(new CreateActivityUserCommand(6L, 4L, null)).isFailure());
  }

  @Test
  void cannotAssignActivityFromDifferentQuest() {
    user(null);
    var a = new Activity(4L, 99L, "Other", 1, ActivityType.CHECKBOX, Map.of(), null);
    when(activities.findById(4L)).thenReturn(Optional.of(a));
    assertTrue(service.handle(new CreateActivityUserCommand(6L, 4L, null)).isFailure());
    verify(assignments, never()).save(any());
  }

  @Test
  void duplicateActivityAssignmentIsRejected() {
    user(null);
    activity();
    when(assignments.existsByQuestUserIdAndActivityId(6L, 4L)).thenReturn(true);
    assertTrue(service.handle(new CreateActivityUserCommand(6L, 4L, null)).isFailure());
  }

  @Test
  void validActivityAssignmentCopiesItsSnapshot() {
    user(null);
    activity();
    when(assignments.save(any())).thenAnswer(i -> i.getArgument(0));
    var a = service.handle(new CreateActivityUserCommand(6L, 4L, null)).toOptional().orElseThrow();
    assertEquals("Check", a.getActivityDescription());
    assertEquals(0.0, a.getProgress());
  }
}
