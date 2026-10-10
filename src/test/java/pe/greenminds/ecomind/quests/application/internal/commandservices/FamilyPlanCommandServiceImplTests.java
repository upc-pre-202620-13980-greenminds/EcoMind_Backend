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
}
