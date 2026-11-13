package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.queryservices.EventChatAccessService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class EventChatAccessServiceImpl implements EventChatAccessService {
    private final EventRepository events;
    private final EventRegistrationRepository registrations;

    public EventChatAccessServiceImpl(
            EventRepository events,
            EventRegistrationRepository registrations) {
        this.events = events;
        this.registrations = registrations;
    }

    @Override
    @Transactional
    public Result<Event, ApplicationError> requireParticipant(
            Long eventId, Long userId) {
        var found = events.findByIdForUpdate(eventId);

        if (found.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Event", String.valueOf(eventId)));
        }

        var event = found.get();

        boolean participant = registrations.findByEventId(eventId).stream()
                .anyMatch(registration -> registration.includes(userId));

        if (!participant) {
            return Result.failure(ApplicationError.forbidden(
                    "EVENT_CHAT_FORBIDDEN",
                    "Only active event participants may access this chat"));
        }

        if (!event.isChatOpen(Instant.now())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_CHAT_EXPIRED",
                    "The event chat has expired"));
        }

        return Result.success(event);
    }
}