package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.*;
import pe.greenminds.ecomind.community.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class CommunityGoalCommandServiceImplTests {

  @Mock CommunityGoalRepository goals;
  @Mock CommunityRepository communities;
  @Mock CommunityMembershipRepository memberships;
  @Mock CommunityAchievementRepository achievements;
  @Mock ApplicationEventPublisher publisher;
  @Mock PostRepository posts;
  @Mock CommunityGoalParticipationRepository participation;

  CommunityGoalCommandServiceImpl service;

  @BeforeEach
  void setup() {
    service = new CommunityGoalCommandServiceImpl(
            goals, communities, memberships, achievements,
            publisher, posts, participation);
  }

  Community topicCommunity() {
    return new Community(
            1L, "Gardening", null, CommunityType.TOPIC,
            "Gardening", null, 100, null, 20L);
  }

  CommunityGoal goal(int progress) {
    return new CommunityGoal(
            3L, 1L, CommunityGoalTopic.ENERGY,
            10, progress, 1, CommunityGoalStatus.ACTIVE);
  }

  void admin() {
    when(communities.findByIdForUpdate(1L))
            .thenReturn(Optional.of(topicCommunity()));
    when(memberships.findByCommunityIdAndUserId(1L, 20L))
            .thenReturn(Optional.of(
                    new CommunityMembership(1L, 1L, 20L, CommunityRole.ADMIN)));
  }

  @Test
  void administratorCreatesCalculatedGoalAndJoins() {
    admin();
    when(memberships.countByCommunityId(1L)).thenReturn(4L);
    when(goals.save(any())).thenAnswer(call -> {
      CommunityGoal candidate = call.getArgument(0);
      return new CommunityGoal(
              3L, candidate.communityId(), candidate.topic(),
              candidate.target(), candidate.progress(),
              candidate.participants(), candidate.status());
    });
    when(participation.enroll(20L, 3L)).thenReturn(true);

    var result = service.handle(
            new CreateCommunityGoalCommand(1L, CommunityGoalTopic.ENERGY, 20L));

    CommunityGoal saved = result.toOptional().orElseThrow();
    assertEquals(40, saved.target());
    assertEquals(1, saved.participants());
    verify(participation).enroll(20L, 3L);
  }

  @Test
  void regularMemberCannotCreateThematicGoal() {
    when(communities.findByIdForUpdate(1L))
            .thenReturn(Optional.of(topicCommunity()));
    when(memberships.findByCommunityIdAndUserId(1L, 20L))
            .thenReturn(Optional.of(
                    new CommunityMembership(1L, 1L, 20L, CommunityRole.MEMBER)));

    assertTrue(service.handle(
            new CreateCommunityGoalCommand(
                    1L, CommunityGoalTopic.WATER, 20L)).isFailure());

    verify(goals, never()).save(any());
  }

  @Test
  void activeGoalBlocksCreation() {
    admin();
    when(goals.existsActiveByCommunityId(1L)).thenReturn(true);

    assertTrue(service.handle(
            new CreateCommunityGoalCommand(
                    1L, CommunityGoalTopic.WATER, 20L)).isFailure());

    verify(goals, never()).save(any());
  }

  @Test
  void localCreatorDoesNotJoinAutomatically() {
    when(communities.findByIdForUpdate(1L))
            .thenReturn(Optional.of(new Community(
                    1L, "Lima", null, CommunityType.LOCAL,
                    null, "Lima", null, null, null)));
    when(memberships.countByCommunityId(1L)).thenReturn(3L);
    when(goals.save(any())).thenAnswer(call -> call.getArgument(0));

    var saved = service.handle(new CreateCommunityGoalCommand(
                    1L, CommunityGoalTopic.WATER, 20L))
            .toOptional().orElseThrow();

    assertEquals(30, saved.target());
    assertEquals(0, saved.participants());
    verify(participation, never()).enroll(anyLong(), anyLong());
  }

  @Test
  void completingGoalCreatesOneAchievementAndPost() {
    Instant now = Instant.now();
    when(participation.findByUserId(20L))
            .thenReturn(Optional.of(
                    new CommunityGoalEnrollment(20L, 3L, now.minusSeconds(60))));
    when(goals.findByIdForUpdate(3L)).thenReturn(Optional.of(goal(9)));
    when(participation.registerCompletion("quest-user:7:20")).thenReturn(true);
    when(memberships.findByCommunityIdAndUserId(1L, 20L))
            .thenReturn(Optional.of(
                    new CommunityMembership(1L, 1L, 20L, CommunityRole.MEMBER)));
    when(participation.countByGoalId(3L)).thenReturn(1L);
    when(goals.save(any())).thenAnswer(call -> call.getArgument(0));

    service.handle(new RecordGoalQuestCompletionCommand(
            "quest-user:7", List.of(20L), CommunityGoalTopic.ENERGY, now));

    verify(goals).save(argThat(goal ->
            goal.progress() == 10 && goal.status() == CommunityGoalStatus.COMPLETED));
    verify(achievements).save(any());
    verify(posts).save(argThat(post ->
            "COMMUNITY_GOAL_COMPLETED".equals(post.postType())));
    verify(participation).releaseGoal(3L);
  }

  @Test
  void duplicateCompletionDoesNotIncrementAgain() {
    Instant now = Instant.now();
    when(participation.findByUserId(20L))
            .thenReturn(Optional.of(
                    new CommunityGoalEnrollment(20L, 3L, now.minusSeconds(60))));
    when(goals.findByIdForUpdate(3L)).thenReturn(Optional.of(goal(5)));
    when(participation.registerCompletion("quest-user:7:20")).thenReturn(false);

    service.handle(new RecordGoalQuestCompletionCommand(
            "quest-user:7", List.of(20L), CommunityGoalTopic.ENERGY, now));

    verify(goals, never()).save(any());
    verifyNoInteractions(achievements, posts, publisher);
  }
}