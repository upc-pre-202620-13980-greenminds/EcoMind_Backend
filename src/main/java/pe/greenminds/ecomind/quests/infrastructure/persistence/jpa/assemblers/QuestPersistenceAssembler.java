package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;


import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.embedddables.RewardPersistenceEmbeddable;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.QuestPersistenceEntity;

public class QuestPersistenceAssembler {
    private QuestPersistenceAssembler(){}

    public static Quest toDomainFromPersistence(QuestPersistenceEntity entity){
        var quest = new Quest(
                entity.getMinigameId(),
                entity.getTitle(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getQuestType(),
                entity.getAge(),
                toDomainFromPersistence(entity.getReward()),
                entity.getTime(),
                entity.getImage(),
                entity.getTheme(),
                entity.getAssignedDate()
        );
        quest.setId(entity.getId());
        return quest;
    }

    public static QuestPersistenceEntity toPersistenceFromDomain(Quest quest){
        var entity = new QuestPersistenceEntity();
        entity.setId(quest.getId());
        entity.setMinigameId(quest.getMinigameId());
        entity.setTitle(quest.getTitle());
        entity.setCategory(quest.getCategory());
        entity.setDescription(quest.getDescription());
        entity.setQuestType(quest.getType());
        entity.setAge(quest.getAge());
        entity.setReward(toPersistenceFromDomain(quest.getReward()));
        entity.setTime(quest.getTime());
        entity.setImage(quest.getImage());
        entity.setTheme(quest.getTheme());
        entity.setAssignedDate(quest.getAssignedDate());
        return entity;
    }

    private static Reward toDomainFromPersistence(RewardPersistenceEmbeddable value) {
        return value == null ? null : new Reward(value.getGemReward(), value.getEcopointReward());
    }

    private static RewardPersistenceEmbeddable toPersistenceFromDomain(Reward value) {
        return value == null ? null : new RewardPersistenceEmbeddable(value.gems(), value.ecopoints());
    }
}
