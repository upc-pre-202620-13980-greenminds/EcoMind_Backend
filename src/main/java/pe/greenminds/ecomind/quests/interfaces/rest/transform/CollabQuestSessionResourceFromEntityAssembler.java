package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestSessionResource;

public final class CollabQuestSessionResourceFromEntityAssembler {
    private CollabQuestSessionResourceFromEntityAssembler() {
    }

    public static CollabQuestSessionResource toResourceFromEntity(
            CollabQuestSession collabQuestSession
    ) {
        if (collabQuestSession == null) {
            return null;
        }

        return new CollabQuestSessionResource(
                collabQuestSession.getId(),
                collabQuestSession.getQuestId(),
                collabQuestSession.getOwnerId(),
                collabQuestSession.getStatus().name(),
                collabQuestSession.getCreatedAt(),
                collabQuestSession.getStartDate(),
                collabQuestSession.getEndDate()
        );
    }
}
