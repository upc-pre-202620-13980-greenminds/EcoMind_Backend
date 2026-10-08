package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.queryservices.EventRegistrationQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventRegistrationsQuery;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@Service
public class EventRegistrationQueryServiceImpl implements EventRegistrationQueryService {
    private final EventRegistrationRepository registrations;
    private final EventRepository events;

    public EventRegistrationQueryServiceImpl(EventRegistrationRepository r, EventRepository e) {
        registrations = r;
        events = e;
    }

    public List<EventRegistration> handle(GetEventRegistrationsQuery q) {
        if (events.findById(q.eventId()).isEmpty())
            throw new IllegalArgumentException("Event not found");
        return registrations.findByEventId(q.eventId());
    }
}
