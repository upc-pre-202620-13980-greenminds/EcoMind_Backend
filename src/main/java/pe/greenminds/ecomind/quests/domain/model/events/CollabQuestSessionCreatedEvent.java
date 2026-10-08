package pe.greenminds.ecomind.quests.domain.model.events;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;

import java.time.LocalDate;

public record CollabQuestSessionCreatedEvent(
        Long questId,
        Long ownerId,
        CollabQuestStatus status,
        LocalDate startDate,
        LocalDate endDate
){
    public static CollabQuestSessionCreatedEvent from(CollabQuestSession collabQuestSession){
        return new CollabQuestSessionCreatedEvent(
                collabQuestSession.getQuestId(),
                collabQuestSession.getOwnerId(),
                collabQuestSession.getStatus(),
                collabQuestSession.getStartDate(),
                collabQuestSession.getEndDate()
        );
    }
}
