package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.CollabQuestSessionPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.CollabQuestSessionPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class CollabQuestSessionRepositoryImpl implements CollabQuestSessionRepository {
    private final CollabQuestSessionPersistenceRepository collabQuestSessionPersistenceRepository;

    public CollabQuestSessionRepositoryImpl(
            CollabQuestSessionPersistenceRepository collabQuestSessionPersistenceRepository
    ) {
        this.collabQuestSessionPersistenceRepository = collabQuestSessionPersistenceRepository;
    }

    @Override
    public CollabQuestSession save(CollabQuestSession collabQuestSession) {
        var savedEntity = collabQuestSessionPersistenceRepository.save(
                CollabQuestSessionPersistenceAssembler.toPersistenceFromDomain(
                        collabQuestSession
                )
        );
        return CollabQuestSessionPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<CollabQuestSession> findById(Long id) {
        return collabQuestSessionPersistenceRepository.findById(id)
                .map(CollabQuestSessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<CollabQuestSession> findByQuestIdAndOwnerUserId(
            Long questId,
            Long ownerUserId
    ) {
        return collabQuestSessionPersistenceRepository
                .findByQuestIdAndOwnerId(questId, ownerUserId)
                .map(CollabQuestSessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<CollabQuestSession> findByQuestIdAndOwnerUserIdAndStatusIn(
            Long questId,
            Long ownerUserId,
            List<CollabQuestStatus> statuses
    ) {
        return collabQuestSessionPersistenceRepository
                .findFirstByQuestIdAndOwnerIdAndStatusInOrderByIdDesc(
                        questId,
                        ownerUserId,
                        statuses
                )
                .map(CollabQuestSessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<CollabQuestSession> findByQuestId(Long questId) {
        return collabQuestSessionPersistenceRepository.findByQuestId(questId)
                .stream()
                .map(CollabQuestSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CollabQuestSession> findByOwnerUserId(Long ownerUserId) {
        return collabQuestSessionPersistenceRepository.findByOwnerId(ownerUserId)
                .stream()
                .map(CollabQuestSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        collabQuestSessionPersistenceRepository.deleteById(id);
    }

    @Override
    public void deleteByQuestId(Long questId) {
        collabQuestSessionPersistenceRepository.deleteByQuestId(questId);
    }
}
