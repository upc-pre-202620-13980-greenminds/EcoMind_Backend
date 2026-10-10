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
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class CollabQuestSessionCommandServiceImplTests {
  @Mock CollabQuestSessionRepository collabQuestSessionRepository;
  @Mock CollabQuestMemberRepository collabQuestMemberRepository;
  @Mock QuestRepository questRepository;
  @Mock QuestUserRepository questUserRepository;
  @Mock ActivityRepository activityRepository;
  @Mock ActivityUserRepository activityUserRepository;
  @Mock FamilyPlanItemRepository familyPlanItemRepository;
  @Mock ApplicationEventPublisher eventPublisher;
  @InjectMocks CollabQuestSessionCommandServiceImpl service;

  CreateCollabQuestSessionCommand command() {
    return new CreateCollabQuestSessionCommand(3L, 20L);
  }

  void quest(QuestType type, boolean published) {
    var q =
        new Quest(
            3L,
            null,
            "Cleanup",
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
    when(questRepository.findById(3L)).thenReturn(Optional.of(q));
  }

  @Test
  void missingQuestCannotStartSession() {
    assertTrue(service.handle(command()).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void individualQuestCannotHaveCollaborativeSession() {
    quest(QuestType.ACTIVITIES, true);
    assertTrue(service.handle(command()).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void unpublishedQuestCannotHaveCollaborativeSession() {
    quest(QuestType.COLLABORATIVE, false);
    assertTrue(service.handle(command()).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void collaborativeQuestNeedsActivities() {
    quest(QuestType.COLLABORATIVE, true);
    when(activityRepository.countByQuestId(3L)).thenReturn(0);
    assertTrue(service.handle(command()).isFailure());
    verifyNoInteractions(collabQuestSessionRepository);
  }

  @Test
  void sessionCreatorIsAcceptedOwner() {
    quest(QuestType.COLLABORATIVE, true);
    when(activityRepository.countByQuestId(3L)).thenReturn(1);
    when(collabQuestSessionRepository.save(any()))
        .thenAnswer(
            c -> {
              CollabQuestSession s = c.getArgument(0);
              s.setId(9L);
              return s;
            });
    assertTrue(service.handle(command()).isSuccess());
    verify(collabQuestMemberRepository)
        .save(
            argThat(
                m ->
                    m.getUserId().equals(20L)
                        && m.getRole() == MemberRole.OWNER
                        && m.getStatus() == CollabMemberStatus.ACCEPTED));
  }

  @Test
  void missingSessionCannotStart() {
    assertTrue(service.handle(new StartCollabQuestSessionCommand(9L, 20L)).isFailure());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void onlyOwnerCanStartSession() {
    var s = new CollabQuestSession(3L, 20L);
    s.setId(9L);
    when(collabQuestSessionRepository.findById(9L)).thenReturn(Optional.of(s));
    assertTrue(service.handle(new StartCollabQuestSessionCommand(9L, 30L)).isFailure());
    verify(collabQuestSessionRepository, never()).save(any());
  }

  @Test
  void startingRequiresTwoAcceptedMembers() {
    var s = new CollabQuestSession(3L, 20L);
    s.setId(9L);
    when(collabQuestSessionRepository.findById(9L)).thenReturn(Optional.of(s));
    when(activityRepository.countByQuestId(3L)).thenReturn(1);
    when(collabQuestMemberRepository.findBySessionIdAndStatusIn(
            9L, List.of(CollabMemberStatus.ACCEPTED)))
        .thenReturn(
            List.of(
                new CollabQuestMember(
                    9L, 20L, 20L, MemberRole.OWNER, CollabMemberStatus.ACCEPTED)));
    assertTrue(service.handle(new StartCollabQuestSessionCommand(9L, 20L)).isFailure());
    verifyNoInteractions(questUserRepository, eventPublisher);
  }
}
