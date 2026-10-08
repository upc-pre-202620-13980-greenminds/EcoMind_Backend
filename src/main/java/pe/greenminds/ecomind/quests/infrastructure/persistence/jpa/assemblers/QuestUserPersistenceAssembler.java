package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.QuestUserPersistenceEntity;

public class QuestUserPersistenceAssembler {
    private QuestUserPersistenceAssembler() {}

    public static QuestUser toDomainFromPersistence(QuestUserPersistenceEntity entity){
        var questUser = new QuestUser(
                entity.getId(),
                entity.getUserId(),
                entity.getQuestId(),
                entity.getStatus(),
                entity.getProgress(),
                entity.getEndDate(),
                entity.getCollaborativeSessionId()
        );
        questUser.setId(entity.getId());
        return questUser;
    }

    public static QuestUserPersistenceEntity toPersistenceFromDomain(QuestUser domain){
        var entity = new QuestUserPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setQuestId(domain.getQuestId());
        entity.setStatus(domain.getStatus());
        entity.setProgress(domain.getProgress());
        entity.setEndDate(domain.getEndDate());
        entity.setCollaborativeSessionId(domain.getCollaborativeSessionId());
        return entity;
    }
}
