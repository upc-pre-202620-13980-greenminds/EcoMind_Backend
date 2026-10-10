package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.events.CommunityGoalCompletedEvent;
import pe.greenminds.ecomind.community.domain.model.valueobjects.*;
import pe.greenminds.ecomind.community.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class CommunityGoalCommandServiceImplTests {
  @Mock CommunityGoalRepository goals;
  @Mock CommunityRepository communities;
  @Mock CommunityMembershipRepository memberships;
  @Mock CommunityAchievementRepository achievements;
  @Mock ApplicationEventPublisher publisher;
  CommunityGoalCommandServiceImpl service;

  @BeforeEach
  void setup() {
    service =
        new CommunityGoalCommandServiceImpl(
            goals, communities, memberships, achievements, publisher);
  }

  void membership(CommunityRole role) {
    when(memberships.findByCommunityIdAndUserId(1L, 20L))
        .thenReturn(Optional.of(new CommunityMembership(1L, 1L, 20L, role)));
  }

  void goal(int progress, CommunityGoalStatus status) {
    when(goals.findById(3L))
        .thenReturn(
            Optional.of(
                new CommunityGoal(
                    3L, 1L, CommunityGoalTopic.ENERGY, 2, progress, progress, status)));
  }

  @Test
  void onlyAdministratorCreatesGoals() {
    membership(CommunityRole.MEMBER);
    assertTrue(
        service
            .handle(new CreateCommunityGoalCommand(1L, CommunityGoalTopic.ENERGY, 2, 20L))
            .isFailure());
    verifyNoInteractions(goals);
  }

  @Test
  void activeGoalBlocksAnotherGoal() {
    membership(CommunityRole.ADMIN);
    when(communities.existsById(1L)).thenReturn(true);
    when(goals.existsActiveByCommunityId(1L)).thenReturn(true);
    assertTrue(
        service
            .handle(new CreateCommunityGoalCommand(1L, CommunityGoalTopic.ENERGY, 2, 20L))
            .isFailure());
    verify(goals, never()).save(any());
  }

  @Test
  void invalidTargetDoesNotPersist() {
    membership(CommunityRole.ADMIN);
    when(communities.existsById(1L)).thenReturn(true);
    assertTrue(
        service
            .handle(new CreateCommunityGoalCommand(1L, CommunityGoalTopic.ENERGY, 0, 20L))
            .isFailure());
    verify(goals, never()).save(any());
  }

  @Test
  void nonMemberCannotContribute() {
    goal(0, CommunityGoalStatus.ACTIVE);
    assertTrue(service.handle(new IncrementCommunityGoalCommand(3L, 20L)).isFailure());
    verify(goals, never()).save(any());
  }

  @Test
  void progressBeforeThresholdDoesNotPublishCompletion() {
    goal(0, CommunityGoalStatus.ACTIVE);
    membership(CommunityRole.MEMBER);
    when(goals.save(any())).thenAnswer(c -> c.getArgument(0));
    var result =
        service.handle(new IncrementCommunityGoalCommand(3L, 20L)).toOptional().orElseThrow();
    assertEquals(1, result.progress());
    assertEquals(CommunityGoalStatus.ACTIVE, result.status());
    verifyNoInteractions(achievements, publisher);
  }

  @Test
  void reachingTargetRecordsAchievementAndCompletion() {
    goal(1, CommunityGoalStatus.ACTIVE);
    membership(CommunityRole.MEMBER);
    when(goals.save(any())).thenAnswer(c -> c.getArgument(0));
    var result =
        service.handle(new IncrementCommunityGoalCommand(3L, 20L)).toOptional().orElseThrow();
    assertEquals(2, result.progress());
    assertEquals(CommunityGoalStatus.COMPLETED, result.status());
    verify(achievements).save(argThat(a -> a.communityGoalId().equals(3L)));
    verify(publisher).publishEvent(isA(CommunityGoalCompletedEvent.class));
  }

  @Test
  void completedGoalRejectsFurtherContributions() {
    goal(2, CommunityGoalStatus.COMPLETED);
    membership(CommunityRole.MEMBER);
    assertTrue(service.handle(new IncrementCommunityGoalCommand(3L, 20L)).isFailure());
    verify(goals, never()).save(any());
    verifyNoInteractions(achievements, publisher);
  }

  @Test
  void missingGoalCannotBeIncremented() {
    assertTrue(service.handle(new IncrementCommunityGoalCommand(3L, 20L)).isFailure());
    verifyNoInteractions(memberships, achievements, publisher);
  }
}
