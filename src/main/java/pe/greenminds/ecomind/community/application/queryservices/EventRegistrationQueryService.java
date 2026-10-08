package pe.greenminds.ecomind.community.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.queries.GetEventRegistrationsQuery;

public interface EventRegistrationQueryService {
    List<EventRegistration> handle(GetEventRegistrationsQuery query);
}
