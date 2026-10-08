package pe.greenminds.ecomind.community.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventsQuery;

public interface EventQueryService {
    List<Event> handle(SearchEventsQuery query);
}
