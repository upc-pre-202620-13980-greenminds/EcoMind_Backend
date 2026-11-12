package pe.greenminds.ecomind.community.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.application.queryservices.EventChatAccessService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventChatMessagesQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventChatPage;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@ExtendWith(MockitoExtension.class)
class EventChatQueryServiceImplTests {
  @Mock EventChatAccessService access;
  @Mock EventChatMessageRepository messages;
  EventChatQueryServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EventChatQueryServiceImpl(access, messages);
  }

  @Test
  void rejectsNullAndNegativeCursorsBeforeCheckingMembership() {
    assertTrue(service.handle(new GetEventChatMessagesQuery(7L, 20L, null)).isFailure());
    assertTrue(service.handle(new GetEventChatMessagesQuery(7L, 20L, -1L)).isFailure());
    verifyNoInteractions(access, messages);
  }

  @Test
  void forwardsAccessFailuresAndDoesNotQueryMessages() {
    when(access.requireParticipant(7L, 20L)).thenReturn(Result.failure(
        ApplicationError.forbidden("EVENT_CHAT_FORBIDDEN", "Not a participant")));
    assertTrue(service.handle(new GetEventChatMessagesQuery(7L, 20L, 0L)).isFailure());
    verifyNoInteractions(messages);
  }

  @Test
  void returnsAuthorizedEventWindowAndMessagesAfterCursor() {
    Event event = new Event(7L, 3L, 30L, "Cleanup", null, LocalDate.now().plusDays(2),
        LocalTime.NOON, "Lima", null, null, 2, null);
    when(access.requireParticipant(7L, 20L)).thenReturn(Result.success(event));
    when(messages.findAfter(7L, 9L)).thenReturn(List.of());
    EventChatPage page = service.handle(new GetEventChatMessagesQuery(7L, 20L, 9L))
        .toOptional().orElseThrow();
    assertEquals(7L, page.eventId());
    assertEquals(30L, page.authorId());
    assertEquals(event.getChatExpiresAt(), page.expiresAt());
    assertTrue(page.messages().isEmpty());
    verify(messages).findAfter(7L, 9L);
  }
}
