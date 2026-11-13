package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.application.queryservices.EventChatAccessService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;
import pe.greenminds.ecomind.community.domain.model.commands.SendEventChatMessageCommand;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@ExtendWith(MockitoExtension.class)
class EventChatCommandServiceImplTests {
  @Mock EventChatAccessService access;
  @Mock EventChatMessageRepository messages;
  @Mock CommunityActorGateway actors;
  EventChatCommandServiceImpl service;
  final UUID clientId = UUID.fromString("11c978fc-dd3a-4089-a60b-3f62265d9733");

  @BeforeEach
  void setUp() {
    service = new EventChatCommandServiceImpl(access, messages, actors);
  }

  @Test
  void missingTextBlankTextLongTextAndMissingClientIdAreRejectedBeforeAccess() {
    assertTrue(service.handle(command(null, clientId)).isFailure());
    assertTrue(service.handle(command("  \n", clientId)).isFailure());
    assertTrue(service.handle(command("x".repeat(2001), clientId)).isFailure());
    assertTrue(service.handle(command("hello", null)).isFailure());
    verifyNoInteractions(access, messages, actors);
  }

  @Test
  void accessFailureIsReturnedWithoutReadingOrWritingMessages() {
    when(access.requireParticipant(7L, 20L)).thenReturn(Result.failure(
        ApplicationError.forbidden("EVENT_CHAT_FORBIDDEN", "Not a participant")));
    assertTrue(service.handle(command("hello", clientId)).isFailure());
    verifyNoInteractions(messages, actors);
  }

  @Test
  void duplicateClientIdWithSameTextReturnsOriginalMessage() {
    EventChatMessage prior = message("hello");
    allowAccess();
    when(messages.findByClientId(7L, 20L, clientId)).thenReturn(Optional.of(prior));
    assertEquals(prior, service.handle(command("  hello  ", clientId)).toOptional().orElseThrow());
    verify(messages, never()).save(any());
    verifyNoInteractions(actors);
  }

  @Test
  void duplicateClientIdCannotChangeMessageText() {
    allowAccess();
    when(messages.findByClientId(7L, 20L, clientId)).thenReturn(Optional.of(message("original")));
    assertTrue(service.handle(command("edited", clientId)).isFailure());
    verify(messages, never()).save(any());
    verifyNoInteractions(actors);
  }

  @Test
  void newMessageIsTrimmedAttributedAndPersisted() {
    allowAccess();
    when(messages.findByClientId(7L, 20L, clientId)).thenReturn(Optional.empty());
    when(actors.getUserName(20L)).thenReturn("Ana");
    when(messages.save(any())).thenAnswer(call -> call.getArgument(0));
    EventChatMessage saved = service.handle(command("  hello neighbors  ", clientId))
        .toOptional().orElseThrow();
    assertEquals(7L, saved.eventId());
    assertEquals(20L, saved.senderId());
    assertEquals("Ana", saved.senderName());
    assertEquals("hello neighbors", saved.text());
    assertEquals(clientId, saved.clientId());
    assertNotNull(saved.sentAt());
    verify(messages).save(any(EventChatMessage.class));
  }

  private void allowAccess() {
    when(access.requireParticipant(7L, 20L)).thenReturn(Result.success(
        new Event(7L, 3L, 30L, "Cleanup", null, LocalDate.now().plusDays(2),
            LocalTime.NOON, "Lima", null, null, 2, null)));
  }

  private SendEventChatMessageCommand command(String text, UUID id) {
    return new SendEventChatMessageCommand(7L, 20L, text, id);
  }

  private EventChatMessage message(String text) {
    return new EventChatMessage(4L, 7L, 20L, "Ana", text, Instant.parse("2027-05-04T15:00:00Z"), clientId);
  }
}
