package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.*;
import pe.greenminds.ecomind.community.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class CommunityCommandServiceImplTests {
  @Mock CommunityRepository communities;
  @Mock CommunityMembershipRepository memberships;
  @Mock CommunityAchievementRepository achievements;
  @Mock CommunityActorGateway actors;
  CommunityCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new CommunityCommandServiceImpl(communities, memberships, achievements, actors);
  }

  Community topic(int capacity) {
    return new Community(
        1L,
        "Gardening",
        "Plant together",
        CommunityType.TOPIC,
        "Gardening",
        null,
        capacity,
        null,
        10L);
  }

  @Test
  void missingCommunityDoesNotCreateMembership() {
    assertTrue(service.handle(new JoinCommunityCommand(1L, 20L)).isFailure());
    verify(memberships, never()).save(any());
  }

  @Test
  void duplicateMembershipIsRejected() {
    when(communities.findById(1L)).thenReturn(Optional.of(topic(10)));
    when(memberships.findByCommunityIdAndUserId(1L, 20L))
        .thenReturn(Optional.of(new CommunityMembership(2L, 1L, 20L, CommunityRole.MEMBER)));
    assertTrue(service.handle(new JoinCommunityCommand(1L, 20L)).isFailure());
    verify(memberships, never()).save(any());
  }

  @Test
  void fullCommunityRejectsAnotherMember() {
    when(communities.findById(1L)).thenReturn(Optional.of(topic(2)));
    when(memberships.countByCommunityId(1L)).thenReturn(2L);
    assertTrue(service.handle(new JoinCommunityCommand(1L, 20L)).isFailure());
    verify(memberships, never()).save(any());
  }

  @Test
  void memberCanJoinAtLastAvailablePlace() {
    when(communities.findById(1L)).thenReturn(Optional.of(topic(2)));
    when(memberships.countByCommunityId(1L)).thenReturn(1L, 2L);
    when(memberships.save(any())).thenAnswer(call -> call.getArgument(0));
    var member = service.handle(new JoinCommunityCommand(1L, 20L)).toOptional().orElseThrow();
    assertEquals(20L, member.userId());
    assertEquals(CommunityRole.MEMBER, member.role());
    verifyNoInteractions(achievements);
  }

  @Test
  void secondLocalCommunityIsRejected() {
    var local =
        new Community(1L, "Neighbors", null, CommunityType.LOCAL, null, "Lima", null, null, 10L);
    when(communities.findById(1L)).thenReturn(Optional.of(local));
    when(communities.findById(2L)).thenReturn(Optional.of(local));
    when(memberships.findByUserId(20L))
        .thenReturn(List.of(new CommunityMembership(3L, 2L, 20L, CommunityRole.MEMBER)));
    assertTrue(service.handle(new JoinCommunityCommand(1L, 20L)).isFailure());
    verify(memberships, never()).save(any());
  }

  @Test
  void thousandthMemberUnlocksOneCommunityAchievement() {
    when(communities.findById(1L)).thenReturn(Optional.of(topic(2000)));
    when(memberships.countByCommunityId(1L)).thenReturn(999L, 1000L);
    when(memberships.save(any())).thenAnswer(call -> call.getArgument(0));
    assertTrue(service.handle(new JoinCommunityCommand(1L, 20L)).isSuccess());
    verify(achievements).save(argThat(a -> a.communityId().equals(1L)));
  }

  @Test
  void studentCannotCreateTopicCommunity() {
    doThrow(new SecurityException("Parent required")).when(actors).requireParent(20L);
    assertTrue(
        service
            .handle(new CreateTopicCommunityCommand("Gardening", null, "Plants", 10, null, 20L))
            .isFailure());
    verifyNoInteractions(communities, memberships);
  }

  @Test
  void invalidLocalityDoesNotPersistCommunity() {
    assertTrue(
        service
            .handle(new CreateLocalCommunityCommand("Neighbors", null, "", null, 20L))
            .isFailure());
    verifyNoInteractions(communities, memberships);
  }
}
