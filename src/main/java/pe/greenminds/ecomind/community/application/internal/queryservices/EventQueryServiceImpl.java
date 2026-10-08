package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.queryservices.EventQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventsQuery;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@Service
public class EventQueryServiceImpl implements EventQueryService {
    private final EventRepository events;

    public EventQueryServiceImpl(EventRepository eventRepository) {
        events = eventRepository;
    }

    @Override
    public List<Event> handle(SearchEventsQuery query) {
        return events.findAll(query.communityId());
    }
}
