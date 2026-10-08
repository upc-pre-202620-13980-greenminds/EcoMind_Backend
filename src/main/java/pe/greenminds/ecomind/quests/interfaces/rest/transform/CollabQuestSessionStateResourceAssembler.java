package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestSessionState;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestCountersResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestPermissionsResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestSessionStateResource;

import java.util.List;

public final class CollabQuestSessionStateResourceAssembler {
    private CollabQuestSessionStateResourceAssembler() {
    }

    public static CollabQuestSessionStateResource toResource(
            CollabQuestSessionState state
    ) {
        var memberResources = state.members()
                .stream()
                .map(CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return new CollabQuestSessionStateResource(
                CollabQuestSessionResourceFromEntityAssembler.toResourceFromEntity(
                        state.session()
                ),
                memberResources,
                state.currentMember() == null
                        ? null
                        : CollabQuestMemberResourceFromEntityAssembler.toResourceFromEntity(
                                state.currentMember()
                        ),
                state.pendingInvitation() == null
                        ? null
                        : CollabQuestMemberResourceFromEntityAssembler.toResourceFromEntity(
                                state.pendingInvitation()
                        ),
                new CollabQuestPermissionsResource(
                        state.permissions().canInvite(),
                        state.permissions().canStart(),
                        state.permissions().canAcceptInvitation(),
                        state.permissions().canLeave(),
                        state.permissions().canRemoveMembers(),
                        state.permissions().canDeleteSession()
                ),
                new CollabQuestCountersResource(
                        state.counters().acceptedInvites(),
                        state.counters().pendingInvites(),
                        state.counters().activeInvites(),
                        state.counters().maxInvites()
                ),
                state.unavailableUserIds(),
                state.source(),
                state.familyPlanId(),
                state.familyPlanItemId()
        );
    }
}
