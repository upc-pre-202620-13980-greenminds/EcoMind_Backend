package pe.greenminds.ecomind.community.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventParticipationQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventParticipation;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventParticipationQueryService {
    Result<List<EventParticipation>, ApplicationError> handle(
            SearchEventParticipationQuery query);
}