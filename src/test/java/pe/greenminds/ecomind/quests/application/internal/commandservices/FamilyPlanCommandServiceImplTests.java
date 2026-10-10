package pe.greenminds.ecomind.quests.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.quests.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.quests.application.internal.queryservices.FamilyPlanStateAssembler;
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class FamilyPlanCommandServiceImplTests {
  @Mock FamilyPlanRepository familyPlanRepository;
  @Mock FamilyPlanItemRepository familyPlanItemRepository;
  @Mock QuestRepository questRepository;
  @Mock ActivityRepository activityRepository;
  @Mock CollabQuestSessionRepository collabQuestSessionRepository;
  @Mock CollabQuestMemberRepository collabQuestMemberRepository;
  @Mock QuestUserRepository questUserRepository;
  @Mock ActivityUserRepository activityUserRepository;
  @Mock FamilyPlanStateAssembler familyPlanStateAssembler;
  @Mock UsersServiceClient usersServiceClient;
  @Mock ApplicationEventPublisher eventPublisher;
  @InjectMocks FamilyPlanCommandServiceImpl service;

  CreateFamilyPlanCommand command() {
    return new CreateFamilyPlanCommand(1L, 20L, List.of(new FamilyPlanItemCommand(3L)));
  }

  void member() {
    when(usersServiceClient.existsUser(20L)).thenReturn(true);
    when(usersServiceClient.isFamilyMember(1L, 20L)).thenReturn(true);
  }

  Quest quest(QuestType type, boolean published) {
    var q =
        new Quest(
            3L,
            null,
            "Family cleanup",
            Category.ENERGY,
            "Check the rooms",
            type,
            8,
            new Reward(2, 10),
            2,
            null,
            Theme.CHECKBOX,
            null);
    if (published) q.publish();
    return q;
  }

  @Test
  void missingUserCannotCreatePlan() {
    assertTrue(service.handle(command()).isFailure());
    verify(familyPlanRepository, never()).save(any());
  }

  @Test
  void outsiderCannotCreatePlanForAnotherFamily() {
    when(usersServiceClient.existsUser(20L)).thenReturn(true);
    assertTrue(service.handle(command()).isFailure());
    verify(familyPlanRepository, never()).save(any());
  }

  @Test
  void missingQuestCannotBeAdded() {
    member();
    assertTrue(service.handle(command()).isFailure());
    verify(familyPlanRepository, never()).save(any());
  }

  @Test
  void individualQuestCannotBeAdded() {
    member();
    when(questRepository.findById(3L)).thenReturn(Optional.of(quest(QuestType.ACTIVITIES, true)));
    assertTrue(service.handle(command()).isFailure());
    verify(familyPlanRepository, never()).save(any());
  }

  @Test
  void draftFamilyQuestCannotBeAdded() {
    member();
    when(questRepository.findById(3L)).thenReturn(Optional.of(quest(QuestType.FAMILY, false)));
    assertTrue(service.handle(command()).isFailure());
    verify(familyPlanRepository, never()).save(any());
  }

  @Test
  void familyQuestNeedsAnActivity() {
    member();
    when(questRepository.findById(3L)).thenReturn(Optional.of(quest(QuestType.FAMILY, true)));
    when(activityRepository.countByQuestId(3L)).thenReturn(0);
    assertTrue(service.handle(command()).isFailure());
    verify(familyPlanRepository, never()).save(any());
  }

  @Test
  void validPlanPersistsSelectedFamilyQuest() {
    member();
    when(questRepository.findById(3L)).thenReturn(Optional.of(quest(QuestType.FAMILY, true)));
    when(activityRepository.countByQuestId(3L)).thenReturn(1);
    when(familyPlanRepository.save(any()))
        .thenAnswer(
            c -> {
              FamilyPlan p = c.getArgument(0);
              p.setId(9L);
              return p;
            });
    assertTrue(service.handle(command()).isSuccess());
    verify(familyPlanItemRepository)
        .save(argThat(i -> i.getQuestId().equals(3L) && i.getFamilyPlanId().equals(9L)));
    verify(familyPlanStateAssembler).toState(any());
  }

  @Test
  void emptyPlanCannotBeActivated() {
    when(familyPlanRepository.findById(9L))
        .thenReturn(Optional.of(new FamilyPlan(9L, 1L, 20L, FamilyPlanStatus.DRAFT)));
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isFailure());
    verify(familyPlanRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void outsiderCannotCompleteActivePlan() {
    when(familyPlanRepository.findById(9L))
        .thenReturn(Optional.of(new FamilyPlan(9L, 1L, 20L, FamilyPlanStatus.ACTIVE)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 30L)).isFailure());
    verify(familyPlanRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void completedPlanCannotBeCompletedAgain() {
    when(familyPlanRepository.findById(9L))
        .thenReturn(Optional.of(new FamilyPlan(9L, 1L, 20L, FamilyPlanStatus.COMPLETED)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
    verify(familyPlanRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }

  FamilyPlan plan(FamilyPlanStatus status) {
    var p = new FamilyPlan(9L, 1L, 20L, status);
    when(familyPlanRepository.findById(9L)).thenReturn(Optional.of(p));
    return p;
  }

  FamilyPlanItem item(Long session) {
    var i = new FamilyPlanItem(9L, 3L);
    i.setId(8L);
    if (session != null) i.attachCollaborativeSession(session);
    when(familyPlanItemRepository.findByFamilyPlanId(9L)).thenReturn(List.of(i));
    return i;
  }

  void validQuest() {
    when(questRepository.findById(3L)).thenReturn(Optional.of(quest(QuestType.FAMILY, true)));
    when(activityRepository.countByQuestId(3L)).thenReturn(1);
  }

  void savePlan() {
    when(familyPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(
      strings = {"update", "activate", "complete", "delete"})
  void missingPlanCannotBeChanged(String operation) {
    var result =
        switch (operation) {
          case "update" -> service.handle(new UpdateFamilyPlanCommand(9L, List.of()));
          case "activate" -> service.handle(new ActivateFamilyPlanCommand(9L));
          case "complete" -> service.handle(new CompleteFamilyPlanCommand(9L, 20L));
          default -> service.handle(new DeleteFamilyPlanCommand(9L));
        };
    assertTrue(result.isFailure());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void draftCanReplaceItsSelectedQuests() {
    plan(FamilyPlanStatus.DRAFT);
    member();
    validQuest();
    assertTrue(
        service
            .handle(new UpdateFamilyPlanCommand(9L, List.of(new FamilyPlanItemCommand(3L))))
            .isSuccess());
    verify(familyPlanItemRepository).deleteByFamilyPlanId(9L);
    verify(familyPlanItemRepository).save(argThat(i -> i.getQuestId().equals(3L)));
  }

  @Test
  void activePlanCannotBeEdited() {
    plan(FamilyPlanStatus.ACTIVE);
    assertTrue(service.handle(new UpdateFamilyPlanCommand(9L, List.of())).isFailure());
    verifyNoInteractions(familyPlanItemRepository);
  }

  @Test
  void invalidEditDoesNotDeleteExistingItems() {
    plan(FamilyPlanStatus.DRAFT);
    assertTrue(
        service
            .handle(new UpdateFamilyPlanCommand(9L, List.of(new FamilyPlanItemCommand(3L))))
            .isFailure());
    verify(familyPlanItemRepository, never()).deleteByFamilyPlanId(any());
  }

  @Test
  void activePlanCannotBeActivatedAgain() {
    plan(FamilyPlanStatus.ACTIVE);
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isFailure());
  }

  @Test
  void missingFamilyCannotActivatePlan() {
    plan(FamilyPlanStatus.DRAFT);
    item(null);
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void ownerMustStillBelongToFamilyWhenActivating() {
    plan(FamilyPlanStatus.DRAFT);
    item(null);
    when(usersServiceClient.getFamilyMemberIds(1L)).thenReturn(List.of(30L));
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void staleQuestBlocksActivationBeforeAnyAssignments() {
    plan(FamilyPlanStatus.DRAFT);
    item(null);
    when(usersServiceClient.getFamilyMemberIds(1L)).thenReturn(List.of(20L));
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void activationCreatesAcceptedMembershipAndActivitiesForEveryFamilyMember() {
    var p = plan(FamilyPlanStatus.DRAFT);
    var i = item(null);
    validQuest();
    savePlan();
    when(usersServiceClient.getFamilyMemberIds(1L)).thenReturn(List.of(20L, 30L));
    when(collabQuestSessionRepository.save(any()))
        .thenAnswer(
            c -> {
              CollabQuestSession v = c.getArgument(0);
              v.setId(40L);
              return v;
            });
    when(questUserRepository.save(any()))
        .thenAnswer(
            c -> {
              QuestUser q = c.getArgument(0);
              q.setId(q.getUserId());
              return q;
            });
    when(activityRepository.findByQuestsIdOrderByOrderAsc(3L))
        .thenReturn(
            List.of(new Activity(5L, 3L, "Check", 1, ActivityType.CHECKBOX, Map.of(), null)));
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isSuccess());
    assertEquals(FamilyPlanStatus.ACTIVE, p.getStatus());
    assertEquals(40L, i.getCollaborativeSessionId());
    verify(collabQuestMemberRepository, times(2))
        .save(argThat(m -> m.getStatus() == CollabMemberStatus.ACCEPTED));
    verify(activityUserRepository, times(2)).save(any());
    verify(eventPublisher)
        .publishEvent(
            any(pe.greenminds.ecomind.quests.domain.model.events.FamilyPlanActivatedEvent.class));
  }

  @Test
  void anActivePersonalAssignmentBlocksActivation() {
    plan(FamilyPlanStatus.DRAFT);
    item(null);
    validQuest();
    when(usersServiceClient.getFamilyMemberIds(1L)).thenReturn(List.of(20L));
    when(questUserRepository.existsByUserIdAndQuestIdAndStatusIn(eq(20L), eq(3L), anyList()))
        .thenReturn(true);
    when(questUserRepository.findByQuestIdAndStatusIn(eq(3L), anyList()))
        .thenReturn(List.of(new QuestUser(20L, 3L, null)));
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isFailure());
    verifyNoInteractions(collabQuestSessionRepository, eventPublisher);
  }

  @Test
  void completedSessionDoesNotBlockNewFamilyActivation() {
    plan(FamilyPlanStatus.DRAFT);
    item(null);
    validQuest();
    savePlan();
    when(usersServiceClient.getFamilyMemberIds(1L)).thenReturn(List.of(20L));
    when(questUserRepository.existsByUserIdAndQuestIdAndStatusIn(eq(20L), eq(3L), anyList()))
        .thenReturn(true);
    when(questUserRepository.findByQuestIdAndStatusIn(eq(3L), anyList()))
        .thenReturn(List.of(new QuestUser(20L, 3L, 50L)));
    when(collabQuestSessionRepository.findById(50L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(50L, 3L, 20L, CollabQuestStatus.COMPLETED, null, null)));
    when(collabQuestSessionRepository.save(any()))
        .thenAnswer(
            c -> {
              CollabQuestSession v = c.getArgument(0);
              v.setId(40L);
              return v;
            });
    when(questUserRepository.save(any()))
        .thenAnswer(
            c -> {
              QuestUser q = c.getArgument(0);
              q.setId(70L);
              return q;
            });
    assertTrue(service.handle(new ActivateFamilyPlanCommand(9L)).isSuccess());
  }

  @Test
  void emptyActivePlanCannotComplete() {
    plan(FamilyPlanStatus.ACTIVE);
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
  }

  @Test
  void unactivatedItemCannotComplete() {
    plan(FamilyPlanStatus.ACTIVE);
    item(null);
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
  }

  @Test
  void missingItemSessionCannotComplete() {
    plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
  }

  @Test
  void cancelledItemSessionCannotComplete() {
    plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    when(collabQuestSessionRepository.findById(40L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.CANCELLED, null, null)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
  }

  @Test
  void itemWithoutAssignmentsCannotComplete() {
    plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    when(collabQuestSessionRepository.findById(40L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.STARTED, null, null)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
  }

  @Test
  void pendingActivitiesBlockCompletion() {
    plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    when(collabQuestSessionRepository.findById(40L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.STARTED, null, null)));
    when(questUserRepository.findByQuestId(3L)).thenReturn(List.of(new QuestUser(20L, 3L, 40L)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void readyMembersCompleteTogetherAndPublishOneFamilyRewardEvent() {
    var p = plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    savePlan();
    var session = new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.STARTED, null, null);
    when(collabQuestSessionRepository.findById(40L)).thenReturn(Optional.of(session));
    var ready = new QuestUser(20L, 3L, 40L);
    ready.setId(60L);
    ready.updateProgress(100.0);
    var completed = new QuestUser(30L, 3L, 40L);
    completed.updateProgress(100.0);
    completed.complete();
    when(questUserRepository.findByQuestId(3L))
        .thenReturn(List.of(ready, completed, new QuestUser(50L, 3L, 99L)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isSuccess());
    assertEquals(FamilyPlanStatus.COMPLETED, p.getStatus());
    assertNotNull(p.getCompletedAt());
    assertEquals(CollabQuestStatus.COMPLETED, session.getStatus());
    verify(questUserRepository).save(ready);
    verify(questUserRepository, never()).save(completed);
    verify(eventPublisher)
        .publishEvent(
            any(pe.greenminds.ecomind.quests.domain.model.events.FamilyPlanCompletedEvent.class));
  }

  @Test
  void completedItemIsNotAwardedAgain() {
    var p = plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    savePlan();
    when(collabQuestSessionRepository.findById(40L))
        .thenReturn(
            Optional.of(
                new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.COMPLETED, null, null)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isSuccess());
    assertEquals(FamilyPlanStatus.COMPLETED, p.getStatus());
    verifyNoInteractions(questUserRepository);
  }

  @Test
  void deletingDraftRemovesItemsAndPlan() {
    plan(FamilyPlanStatus.DRAFT);
    assertTrue(service.handle(new DeleteFamilyPlanCommand(9L)).isSuccess());
    verify(familyPlanItemRepository).deleteByFamilyPlanId(9L);
    verify(familyPlanRepository).deleteById(9L);
  }

  @Test
  void completedPlanCannotBeDeleted() {
    plan(FamilyPlanStatus.COMPLETED);
    assertTrue(service.handle(new DeleteFamilyPlanCommand(9L)).isFailure());
    verify(familyPlanRepository, never()).deleteById(any());
  }

  @Test
  void cancellationRemovesOnlyUnfinishedProgressAndCancelsItsSession() {
    var p = plan(FamilyPlanStatus.ACTIVE);
    item(40L);
    savePlan();
    var session = new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.STARTED, null, null);
    when(collabQuestSessionRepository.findById(40L)).thenReturn(Optional.of(session));
    var open = new QuestUser(20L, 3L, 40L);
    open.setId(60L);
    var completed = new QuestUser(30L, 3L, 40L);
    completed.setId(61L);
    completed.updateProgress(100.0);
    completed.complete();
    when(questUserRepository.findByQuestId(3L))
        .thenReturn(List.of(open, completed, new QuestUser(50L, 3L, 99L)));
    assertTrue(service.handle(new DeleteFamilyPlanCommand(9L)).isSuccess());
    assertEquals(FamilyPlanStatus.CANCELLED, p.getStatus());
    assertEquals(CollabQuestStatus.CANCELLED, session.getStatus());
    verify(activityUserRepository).deleteByQuestUserId(60L);
    verify(questUserRepository).deleteById(60L);
    verify(questUserRepository, never()).deleteById(61L);
  }

  @Test
  void laterIncompleteItemMustNotCompleteEarlierReadyItem() {
    plan(FamilyPlanStatus.ACTIVE);
    var first = new FamilyPlanItem(9L, 3L);
    first.attachCollaborativeSession(40L);
    var second = new FamilyPlanItem(9L, 4L);
    second.attachCollaborativeSession(41L);
    when(familyPlanItemRepository.findByFamilyPlanId(9L)).thenReturn(List.of(first, second));
    var firstSession = new CollabQuestSession(40L, 3L, 20L, CollabQuestStatus.STARTED, null, null);
    var secondSession = new CollabQuestSession(41L, 4L, 20L, CollabQuestStatus.STARTED, null, null);
    when(collabQuestSessionRepository.findById(40L)).thenReturn(Optional.of(firstSession));
    when(collabQuestSessionRepository.findById(41L)).thenReturn(Optional.of(secondSession));
    var ready = new QuestUser(20L, 3L, 40L);
    ready.updateProgress(100.0);
    when(questUserRepository.findByQuestId(3L)).thenReturn(List.of(ready));
    when(questUserRepository.findByQuestId(4L)).thenReturn(List.of(new QuestUser(20L, 4L, 41L)));
    assertTrue(service.handle(new CompleteFamilyPlanCommand(9L, 20L)).isFailure());
    assertEquals(QuestStatus.READY_TO_COMPLETE, ready.getStatus());
    assertEquals(CollabQuestStatus.STARTED, firstSession.getStatus());
    verify(questUserRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }
}
