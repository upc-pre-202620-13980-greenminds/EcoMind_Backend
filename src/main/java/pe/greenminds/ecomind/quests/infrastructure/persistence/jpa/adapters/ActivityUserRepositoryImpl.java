package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.ActivityUserPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.ActivityUserPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivityUserRepositoryImpl implements ActivityUserRepository {
    private final ActivityUserPersistenceRepository activityUserPersistenceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ActivityUserRepositoryImpl(
            ActivityUserPersistenceRepository activityUserPersistenceRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.activityUserPersistenceRepository = activityUserPersistenceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public ActivityUser save(ActivityUser activityUser) {
        boolean isNew = activityUser.getId() == null;
        var savedEntity = activityUserPersistenceRepository.save(
                ActivityUserPersistenceAssembler.toPersistenceFromDomain(activityUser)
        );
        var savedActivityUser =
                ActivityUserPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedActivityUser.onCreated();
            savedActivityUser.domainEvents().forEach(applicationEventPublisher::publishEvent);
            savedActivityUser.clearDomainEvents();
        }

        return savedActivityUser;
    }

    @Override
    public Optional<ActivityUser> findById(Long id) {
        return activityUserPersistenceRepository.findById(id)
                .map(ActivityUserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<ActivityUser> findByQuestUserIdAndActivityId(
            Long questUserId,
            Long activityId
    ) {
        return activityUserPersistenceRepository
                .findByQuestUserIdAndActivityId(questUserId, activityId)
                .map(ActivityUserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ActivityUser> findByQuestUserId(Long questUserId) {
        return activityUserPersistenceRepository.findByQuestUserId(questUserId)
                .stream()
                .map(ActivityUserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        activityUserPersistenceRepository.deleteById(id);
    }

    @Override
    public void deleteByQuestUserId(Long questUserId) {
        activityUserPersistenceRepository.deleteByQuestUserId(questUserId);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        activityUserPersistenceRepository.deleteByActivityId(activityId);
    }

    @Override
    public boolean existsByQuestUserIdAndActivityId(Long questUserId, Long activityId) {
        return activityUserPersistenceRepository
                .existsByQuestUserIdAndActivityId(questUserId, activityId);
    }

    @Override
    public long countByQuestUserId(Long questUserId) {
        return activityUserPersistenceRepository.countByQuestUserId(questUserId);
    }
}
