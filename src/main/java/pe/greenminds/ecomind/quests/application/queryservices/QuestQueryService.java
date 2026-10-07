package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.queries.GetAllQuestsQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.SearchQuestQuery;

import java.util.List;
import java.util.Optional;

public interface QuestQueryService {

    Optional<Quest> handle(GetQuestByIdQuery query);

    List<Quest> handle(GetAllQuestsQuery query);

    List<Quest> handle(SearchQuestQuery query);
}
