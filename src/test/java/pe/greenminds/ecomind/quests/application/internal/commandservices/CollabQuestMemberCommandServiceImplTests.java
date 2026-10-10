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
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.quests.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.quests.domain.model.aggregates.*;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.model.events.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class CollabQuestMemberCommandServiceImplTests {
  @Mock CollabQuestMemberRepository members;
  @Mock CollabQuestSessionRepository sessions;
  @Mock QuestUserRepository progress;
  @Mock ActivityUserRepository activities;
  @Mock FamilyPlanItemRepository plans;
  @Mock UsersServiceClient users;
  @Mock ApplicationEventPublisher events;
  @InjectMocks CollabQuestMemberCommandServiceImpl service;

  CollabQuestSession session(CollabQuestStatus status) {
    var s = new CollabQuestSession(9L, 3L, 20L, status, null, null);
    when(sessions.findById(9L)).thenReturn(Optional.of(s));
    return s;
  }

  CollabQuestMember member(CollabMemberStatus status, MemberRole role) {
    var m = new CollabQuestMember(9L, role == MemberRole.OWNER ? 20L : 30L, 20L, role, status);
    m.setId(7L);
    when(members.findById(7L)).thenReturn(m);
    return m;
  }

  void saveMember() {
    when(members.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  void eligibleInvite(boolean friend) {
    session(CollabQuestStatus.PENDING);
    when(users.existsUser(30L)).thenReturn(true);
    when(users.areFriends(20L, 30L)).thenReturn(friend);
    if (!friend) when(users.belongToSameFamily(20L, 30L)).thenReturn(true);
  }

  @Test
  void missingSessionRejectsInvitation() {
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verifyNoInteractions(users, events);
  }

  @Test
  void familyPlanCannotReceiveManualInvitations() {
    session(CollabQuestStatus.PENDING);
    when(plans.existsByCollaborativeSessionId(9L)).thenReturn(true);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verifyNoInteractions(users, events);
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabQuestStatus.class,
      names = {"STARTED", "COMPLETED", "CANCELLED"})
  void closedInvitationWindowRejectsMembers(CollabQuestStatus status) {
    session(status);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verifyNoInteractions(users, events);
  }

  @Test
  void ownerCannotInviteThemself() {
    session(CollabQuestStatus.PENDING);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 20L)).isFailure());
  }

  @Test
  void anotherUserCannotInvite() {
    session(CollabQuestStatus.PENDING);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 40L, 30L)).isFailure());
    verifyNoInteractions(users, events);
  }

  @Test
  void missingInviteeIsRejected() {
    session(CollabQuestStatus.PENDING);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verify(members, never()).save(any());
  }

  @Test
  void strangerCannotBeInvited() {
    session(CollabQuestStatus.PENDING);
    when(users.existsUser(30L)).thenReturn(true);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verify(members, never()).save(any());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void friendOrFamilyMemberReceivesPendingInvitation(boolean friend) {
    eligibleInvite(friend);
    saveMember();
    var result =
        service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).toOptional().orElseThrow();
    assertEquals(CollabMemberStatus.PENDING, result.getStatus());
    assertEquals(30L, result.getUserId());
    verify(events).publishEvent(any(CollaborativeQuestInvitationSentEvent.class));
  }

  @Test
  void duplicateInvitationIsRejected() {
    eligibleInvite(true);
    when(members.existsBySessionIdAndUserId(9L, 30L)).thenReturn(true);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verifyNoInteractions(events);
  }

  @Test
  void acceptedInvitationInAnotherSessionBlocksInvite() {
    eligibleInvite(true);
    when(members.findByUserIdAndQuestIdAndSessionStatusIn(eq(30L), eq(3L), anyList()))
        .thenReturn(
            List.of(
                new CollabQuestMember(
                    10L, 30L, 40L, MemberRole.PARTICIPANT, CollabMemberStatus.ACCEPTED)));
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verify(members, never()).save(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"accept", "decline", "leave", "remove"})
  void missingMembershipCannotBeChanged(String operation) {
    var result =
        switch (operation) {
          case "accept" -> service.handle(new AcceptCollabQuestMemberCommand(7L));
          case "decline" -> service.handle(new DeclineCollabQuestMemberCommand(7L));
          case "leave" -> service.handle(new LeaveCollabQuestMemberCommand(7L));
          default -> service.handle(new RemoveCollabQuestMemberCommand(7L, 20L));
        };
    assertTrue(result.isFailure());
    verifyNoInteractions(events);
  }

  @ParameterizedTest
  @ValueSource(strings = {"accept", "decline"})
  void ownerCannotAnswerAnInvitation(String operation) {
    member(CollabMemberStatus.ACCEPTED, MemberRole.OWNER);
    assertTrue(
        (operation.equals("accept")
                ? service.handle(new AcceptCollabQuestMemberCommand(7L))
                : service.handle(new DeclineCollabQuestMemberCommand(7L)))
            .isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabMemberStatus.class,
      names = {"ACCEPTED", "REJECTED", "LEFT"})
  void answeredInvitationCannotBeAcceptedAgain(CollabMemberStatus status) {
    member(status, MemberRole.PARTICIPANT);
    assertTrue(service.handle(new AcceptCollabQuestMemberCommand(7L)).isFailure());
    verifyNoInteractions(events);
  }

  @Test
  void acceptanceRequiresSession() {
    member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    assertTrue(service.handle(new AcceptCollabQuestMemberCommand(7L)).isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabQuestStatus.class,
      names = {"STARTED", "COMPLETED", "CANCELLED"})
  void acceptanceRequiresPendingSession(CollabQuestStatus status) {
    member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(status);
    assertTrue(service.handle(new AcceptCollabQuestMemberCommand(7L)).isFailure());
  }

  @Test
  void acceptedMembershipElsewhereBlocksAcceptance() {
    member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.PENDING);
    var other =
        new CollabQuestMember(10L, 30L, 40L, MemberRole.PARTICIPANT, CollabMemberStatus.ACCEPTED);
    other.setId(8L);
    when(members.findByUserIdAndQuestIdAndSessionStatusIn(eq(30L), eq(3L), anyList()))
        .thenReturn(List.of(other));
    assertTrue(service.handle(new AcceptCollabQuestMemberCommand(7L)).isFailure());
    verifyNoInteractions(events);
  }

  @Test
  void existingActiveAssignmentBlocksAcceptance() {
    member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.PENDING);
    when(progress.findFirstByUserIdAndQuestIdAndStatusIn(eq(30L), eq(3L), anyList()))
        .thenReturn(Optional.of(new QuestUser(30L, 3L, 10L)));
    assertTrue(service.handle(new AcceptCollabQuestMemberCommand(7L)).isFailure());
    verifyNoInteractions(events);
  }

  @Test
  void acceptancePersistsAnswerAndPublishesOnce() {
    var m = member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.PENDING);
    saveMember();
    assertTrue(service.handle(new AcceptCollabQuestMemberCommand(7L)).isSuccess());
    assertEquals(CollabMemberStatus.ACCEPTED, m.getStatus());
    assertNotNull(m.getAnswerDate());
    verify(events).publishEvent(any(CollaborativeQuestInvitationAcceptedEvent.class));
  }

  @Test
  void declinePersistsRejectionAndPublishesEvent() {
    var m = member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    saveMember();
    assertTrue(service.handle(new DeclineCollabQuestMemberCommand(7L)).isSuccess());
    assertEquals(CollabMemberStatus.REJECTED, m.getStatus());
    assertNotNull(m.getRevokeDate());
    verify(events).publishEvent(any(CollaborativeQuestInvitationRejectedEvent.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"accept", "decline", "leave", "remove"})
  void familyPlanMembershipCannotBeChangedIndividually(String operation) {
    member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    if (!operation.equals("decline")) session(CollabQuestStatus.PENDING);
    when(plans.existsByCollaborativeSessionId(9L)).thenReturn(true);
    var result =
        switch (operation) {
          case "accept" -> service.handle(new AcceptCollabQuestMemberCommand(7L));
          case "decline" -> service.handle(new DeclineCollabQuestMemberCommand(7L));
          case "leave" -> service.handle(new LeaveCollabQuestMemberCommand(7L));
          default -> service.handle(new RemoveCollabQuestMemberCommand(7L, 20L));
        };
    assertTrue(result.isFailure());
    verify(members, never()).save(any());
  }

  @Test
  void leavingPendingInvitationDeclinesIt() {
    var m = member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.PENDING);
    saveMember();
    assertTrue(service.handle(new LeaveCollabQuestMemberCommand(7L)).isSuccess());
    assertEquals(CollabMemberStatus.REJECTED, m.getStatus());
    verifyNoInteractions(activities, progress);
  }

  @Test
  void ownerCannotLeavePendingSession() {
    member(CollabMemberStatus.ACCEPTED, MemberRole.OWNER);
    session(CollabQuestStatus.PENDING);
    assertTrue(service.handle(new LeaveCollabQuestMemberCommand(7L)).isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabQuestStatus.class,
      names = {"COMPLETED", "CANCELLED"})
  void completedOrCancelledSessionCannotBeLeft(CollabQuestStatus status) {
    member(CollabMemberStatus.ACCEPTED, MemberRole.PARTICIPANT);
    session(status);
    assertTrue(service.handle(new LeaveCollabQuestMemberCommand(7L)).isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabMemberStatus.class,
      names = {"PENDING", "REJECTED", "LEFT"})
  void onlyAcceptedParticipantMayLeaveStartedSession(CollabMemberStatus status) {
    member(status, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.STARTED);
    assertTrue(service.handle(new LeaveCollabQuestMemberCommand(7L)).isFailure());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void leavingDeletesOnlyOwnProgressAndCancelsOnlyEmptySession(boolean remaining) {
    var m = member(CollabMemberStatus.ACCEPTED, MemberRole.PARTICIPANT);
    var s = session(CollabQuestStatus.STARTED);
    saveMember();
    var q = new QuestUser(30L, 3L, 9L);
    q.setId(15L);
    when(progress.findFirstByUserIdAndQuestIdAndStatusIn(eq(30L), eq(3L), anyList()))
        .thenReturn(Optional.of(q));
    when(members.findBySessionIdAndStatusIn(eq(9L), anyList()))
        .thenReturn(
            remaining
                ? List.of(
                    new CollabQuestMember(
                        9L, 20L, 20L, MemberRole.OWNER, CollabMemberStatus.ACCEPTED))
                : List.of());
    assertTrue(service.handle(new LeaveCollabQuestMemberCommand(7L)).isSuccess());
    assertEquals(CollabMemberStatus.LEFT, m.getStatus());
    verify(activities).deleteByQuestUserId(15L);
    verify(progress).deleteById(15L);
    assertEquals(
        remaining ? CollabQuestStatus.STARTED : CollabQuestStatus.CANCELLED, s.getStatus());
  }

  @Test
  void leavingWithoutAssignmentStillClosesEmptySession() {
    member(CollabMemberStatus.ACCEPTED, MemberRole.PARTICIPANT);
    var s = session(CollabQuestStatus.STARTED);
    saveMember();
    assertTrue(service.handle(new LeaveCollabQuestMemberCommand(7L)).isSuccess());
    assertEquals(CollabQuestStatus.CANCELLED, s.getStatus());
    verifyNoInteractions(activities);
  }

  @Test
  void ownerRevokesPendingInvitation() {
    var m = member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.PENDING);
    saveMember();
    assertTrue(service.handle(new RemoveCollabQuestMemberCommand(7L, 20L)).isSuccess());
    assertEquals(CollabMemberStatus.REJECTED, m.getStatus());
    assertNotNull(m.getRevokeDate());
  }

  @Test
  void nonOwnerCannotRemoveParticipant() {
    member(CollabMemberStatus.PENDING, MemberRole.PARTICIPANT);
    session(CollabQuestStatus.PENDING);
    assertTrue(service.handle(new RemoveCollabQuestMemberCommand(7L, 40L)).isFailure());
    verify(members, never()).save(any());
  }

  @Test
  void ownerCannotRemoveThemself() {
    member(CollabMemberStatus.ACCEPTED, MemberRole.OWNER);
    session(CollabQuestStatus.PENDING);
    assertTrue(service.handle(new RemoveCollabQuestMemberCommand(7L, 20L)).isFailure());
  }

  @ParameterizedTest
  @EnumSource(
      value = CollabQuestStatus.class,
      names = {"STARTED", "COMPLETED", "CANCELLED"})
  void removalRequiresPendingSession(CollabQuestStatus status) {
    member(CollabMemberStatus.ACCEPTED, MemberRole.PARTICIPANT);
    session(status);
    assertTrue(service.handle(new RemoveCollabQuestMemberCommand(7L, 20L)).isFailure());
  }

  @Test
  void invitationLimitIncludesOwnerAndPendingMembers() {
    session(CollabQuestStatus.PENDING);
    var active =
        java.util.stream.LongStream.rangeClosed(20, 24)
            .mapToObj(
                id ->
                    new CollabQuestMember(
                        9L, id, 20L, MemberRole.PARTICIPANT, CollabMemberStatus.PENDING))
            .toList();
    when(members.findBySessionIdAndStatusIn(eq(9L), anyList())).thenReturn(active);
    assertTrue(service.handle(new InviteCollabQuestMemberCommand(9L, 20L, 30L)).isFailure());
    verify(members, never()).save(any());
  }
}
