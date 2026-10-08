package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMemberByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMembersBySessionQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMembersByUserQuery;

import java.util.List;
import java.util.Optional;

public interface CollabQuestMemberQueryService {
    Optional<CollabQuestMember> handle(GetCollabQuestMemberByIdQuery query);
    List<CollabQuestMember> handle(GetCollabQuestMembersBySessionQuery query);
    List<CollabQuestMember> handle(GetCollabQuestMembersByUserQuery query);
}
