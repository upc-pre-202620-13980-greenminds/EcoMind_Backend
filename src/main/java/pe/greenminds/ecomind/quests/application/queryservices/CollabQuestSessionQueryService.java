package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestSessionStateQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestSessionStateBySessionIdQuery;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface CollabQuestSessionQueryService {
    CollabQuestSessionState handle(GetCollabQuestSessionStateQuery query);
    Result<CollabQuestSessionState, ApplicationError> handle(GetCollabQuestSessionStateBySessionIdQuery query);
}
