package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.repositories.QuestExperienceRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.QuestExperiencePersistenceEntity;

import java.util.OptionalLong;

@Repository
public class QuestExperienceRepositoryImpl implements QuestExperienceRepository {
    private final EntityManager entities;

    public QuestExperienceRepositoryImpl(EntityManager entities) {
        this.entities = entities;
    }

    public OptionalLong find(Long id) {
        var row = entities.find(QuestExperiencePersistenceEntity.class, id);
        return row == null ? OptionalLong.empty() : OptionalLong.of(row.getExperience());
    }

    public void configure(Long id, long xp) {
        var row =
                entities.find(
                        QuestExperiencePersistenceEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (row == null) {
            row = new QuestExperiencePersistenceEntity();
            row.setQuestId(id);
            entities.persist(row);
        }
        row.setExperience(xp);
    }
}
