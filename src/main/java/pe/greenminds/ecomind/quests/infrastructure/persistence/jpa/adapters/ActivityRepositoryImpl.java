package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.ActivityPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.ActivityPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRepositoryImpl implements ActivityRepository {
    private final ActivityPersistenceRepository activityPersistenceRepository;

    public ActivityRepositoryImpl(ActivityPersistenceRepository activityPersistenceRepository) {
        this.activityPersistenceRepository = activityPersistenceRepository;
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return activityPersistenceRepository.findById(id).map(ActivityPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Activity save(Activity activity) {
        var savedEntity = activityPersistenceRepository.save(ActivityPersistenceAssembler.toPersistenceFromDomain(activity));
        return ActivityPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public List<Activity> findByQuestsIdOrderByOrderAsc(Long questId){
        return activityPersistenceRepository.findByQuestIdOrderByOrderAsc(questId).stream().map(ActivityPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public boolean existsByQuestIdAndOrder(Long questId, Integer order){
        return activityPersistenceRepository.existsByQuestIdAndOrder(questId, order);
    }

    @Override
    public void deleteById(Long id) {
        activityPersistenceRepository.deleteById(id);
    }

    @Override
    public void deleteByQuestId(Long questId) {
        activityPersistenceRepository.deleteByQuestId(questId);
    }

    @Override
    public Integer countByQuestId(Long questId) {
        return activityPersistenceRepository.countByQuestId(questId);
    }
}
