package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestSessionStateQuery;

public interface CollabQuestSessionQueryService {
    CollabQuestSessionState handle(GetCollabQuestSessionStateQuery query);
}
