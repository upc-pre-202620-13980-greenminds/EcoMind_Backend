package pe.greenminds.ecomind.community.application.internal.commandservices;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.EventLifecycleService;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@Service
public class EventLifecycleServiceImpl implements EventLifecycleService {
    private final EventRepository events;
    private final EventRegistrationRepository registrations;
    private final EventChatMessageRepository messages;

    public EventLifecycleServiceImpl(
            EventRepository events,
            EventRegistrationRepository registrations,
            EventChatMessageRepository messages) {
        this.events = events;
        this.registrations = registrations;
        this.messages = messages;
    }

    @Override
    @Transactional
    public void delete(Long eventId) {
        var event = events.findByIdForUpdate(eventId);

        if (event.isEmpty()) {
            return;
        }

        messages.deleteByEventId(eventId);
        registrations.deleteByEventId(eventId);
        events.delete(event.get());
    }

    @Override
    @Transactional
    public void expireMessages(Long eventId) {
        var event = events.findByIdForUpdate(eventId);

        if (event.isPresent() && !event.get().isChatOpen(Instant.now())) {
            messages.deleteByEventId(eventId);
        }
    }
}