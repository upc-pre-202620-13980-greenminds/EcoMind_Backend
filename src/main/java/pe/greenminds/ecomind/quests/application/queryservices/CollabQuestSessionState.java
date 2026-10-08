package pe.greenminds.ecomind.quests.application.queryservices;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;

import java.util.List;

public record CollabQuestSessionState(
        CollabQuestSession session,
        List<CollabQuestMember> members,
        CollabQuestMember currentMember,
        CollabQuestMember pendingInvitation,
        CollabQuestPermissions permissions,
        CollabQuestCounters counters,
        List<Long> unavailableUserIds,
        CollabQuestSource source,
        Long familyPlanId,
        Long familyPlanItemId
) {
}
