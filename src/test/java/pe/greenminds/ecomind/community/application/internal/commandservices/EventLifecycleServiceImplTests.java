package pe.greenminds.ecomind.community.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventLifecycleServiceImplTests {
  @Mock EventRepository events;
  @Mock EventRegistrationRepository registrations;
  @Mock EventChatMessageRepository messages;
  @InjectMocks EventLifecycleServiceImpl service;

  private Event event() {
    return new Event(
        2L,
        1L,
        20L,
        "Cleanup",
        "Park",
        LocalDate.now().plusDays(2),
        LocalTime.NOON,
        "Lima",
        -12.0,
        -77.0,
        10,
        null);
  }

  @Test
  void deleteRemovesMessagesRegistrationsAndEventInForeignKeyOrder() {
    var event = event();
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.of(event));

    assertDoesNotThrow(() -> service.delete(2L));

    InOrder deletionOrder = inOrder(messages, registrations, events);
    deletionOrder.verify(messages).deleteByEventId(2L);
    deletionOrder.verify(registrations).deleteByEventId(2L);
    deletionOrder.verify(events).delete(event);
  }

  @Test
  void deletingMissingEventDoesNotDeleteDependentRows() {
    when(events.findByIdForUpdate(2L)).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> service.delete(2L));

    verify(events).findByIdForUpdate(2L);
    verify(events, never()).delete(any());
    verifyNoInteractions(messages, registrations);
  }
}
