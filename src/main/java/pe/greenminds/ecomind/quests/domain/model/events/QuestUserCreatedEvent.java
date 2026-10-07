package pe.greenminds.ecomind.quests.domain.model.events;

import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;

import java.time.LocalDate;

public record QuestUserCreatedEvent(
        Long id,
        Long userId,
        Long questId,
        QuestStatus status,
        Double progress,
        LocalDate endDate,
        Long CollaborativeSessionId
) {
    public static QuestUserCreatedEvent from(QuestUser questUser){
        return new QuestUserCreatedEvent(
                questUser.getId(),
                questUser.getUserId(),
                questUser.getQuestId(),
                questUser.getStatus(),
                questUser.getProgress(),
                questUser.getEndDate(),
                questUser.getCollaborativeSessionId()
        );
    }
}
