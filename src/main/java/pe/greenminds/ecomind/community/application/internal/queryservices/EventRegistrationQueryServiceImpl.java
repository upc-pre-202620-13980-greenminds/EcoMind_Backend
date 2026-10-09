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

    public EventRegistrationQueryServiceImpl(EventRegistrationRepository eventRegistrationRepository, EventRepository eventRepository) {
        registrations = eventRegistrationRepository;
        events = eventRepository;
    }

    @Override
    public List<EventRegistration> handle(GetEventRegistrationsQuery query) {
        if (events.findById(query.eventId()).isEmpty())
            throw new IllegalArgumentException("Event not found");
        return registrations.findByEventId(query.eventId());
    }
}
