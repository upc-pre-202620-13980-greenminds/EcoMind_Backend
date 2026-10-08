package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.MinigameAttemptPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.MinigameAttemptPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class MinigameAttemptRepositoryImpl implements MinigameAttemptRepository {
    private final MinigameAttemptPersistenceRepository minigameAttemptPersistenceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MinigameAttemptRepositoryImpl(
            MinigameAttemptPersistenceRepository minigameAttemptPersistenceRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.minigameAttemptPersistenceRepository = minigameAttemptPersistenceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public MinigameAttempt save(MinigameAttempt minigameAttempt) {
        var pendingEvents = minigameAttempt.domainEvents();
        var savedAttempt = MinigameAttemptPersistenceAssembler.toDomainFromPersistence(
                minigameAttemptPersistenceRepository.save(
                        MinigameAttemptPersistenceAssembler.toPersistenceFromDomain(
                                minigameAttempt
                        )
                )
        );
        pendingEvents.forEach(applicationEventPublisher::publishEvent);
        minigameAttempt.clearDomainEvents();
        return savedAttempt;
    }

    @Override
    public Optional<MinigameAttempt> findById(Long id) {
        return minigameAttemptPersistenceRepository.findById(id)
                .map(MinigameAttemptPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByUserIdAndStatus(Long userId, MinigameAttemptStatus status) {
        return minigameAttemptPersistenceRepository.existsByUserIdAndStatus(userId, status);
    }

    @Override
    public List<MinigameAttempt> findByUserIdAndMinigameId(Long userId, Long minigameId) {
        return minigameAttemptPersistenceRepository
                .findByUserIdAndMinigameIdOrderByStartDateDesc(userId, minigameId)
                .stream()
                .map(MinigameAttemptPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public void deleteByMinigameId(Long minigameId) {
        minigameAttemptPersistenceRepository.deleteByMinigameId(minigameId);
    }
}
