package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByUserIdAndQuestIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUsersByUserIdAndStatusQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserVersionStatusQuery;

import java.util.List;
import java.util.Optional;

public interface QuestUserQueryService {
    Optional<QuestUser> handle(GetQuestUserByIdQuery query);

    Optional<QuestUser> handle(GetQuestUserByUserIdAndQuestIdQuery query);

    List<QuestUser> handle(GetQuestUsersByUserIdAndStatusQuery query);

    Optional<QuestUserVersionStatus> handle(GetQuestUserVersionStatusQuery query);
}
