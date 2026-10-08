package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.CollabQuestMemberPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.CollabQuestMemberPersistenceRepository;

import java.util.List;

@Repository
public class CollabQuestMemberRepositoryImpl implements CollabQuestMemberRepository {
    private final CollabQuestMemberPersistenceRepository collabQuestMemberPersistenceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CollabQuestMemberRepositoryImpl(
            CollabQuestMemberPersistenceRepository collabQuestMemberPersistenceRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.collabQuestMemberPersistenceRepository = collabQuestMemberPersistenceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public CollabQuestMember save(CollabQuestMember collabQuestMember) {
        boolean isNew = collabQuestMember.getId() == null;
        var pendingEvents = collabQuestMember.domainEvents();
        var savedEntity = collabQuestMemberPersistenceRepository.save(
                CollabQuestMemberPersistenceAssembler.toPersistenceFromDomain(collabQuestMember)
        );
        var savedCollabQuestMember =
                CollabQuestMemberPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedCollabQuestMember.onCreated();
            savedCollabQuestMember.domainEvents().forEach(applicationEventPublisher::publishEvent);
            savedCollabQuestMember.clearDomainEvents();
        }
        pendingEvents.forEach(applicationEventPublisher::publishEvent);
        collabQuestMember.clearDomainEvents();

        return savedCollabQuestMember;
    }

    @Override
    public boolean existsById(Long id) {
        return collabQuestMemberPersistenceRepository.existsById(id);
    }

    @Override
    public boolean existsBySessionIdAndUserId(Long sessionId, Long userId) {
        return collabQuestMemberPersistenceRepository.existsBySessionIdAndUserId(
                sessionId,
                userId
        );
    }

    @Override
    public CollabQuestMember findById(Long id) {
        return collabQuestMemberPersistenceRepository.findById(id)
                .map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .orElse(null);
    }

    @Override
    public CollabQuestMember findBySessionIdAndUserId(Long sessionId, Long userId) {
        return collabQuestMemberPersistenceRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .orElse(null);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        collabQuestMemberPersistenceRepository.deleteBySessionId(sessionId);
    }

    @Override
    public List<CollabQuestMember> findBySessionId(Long sessionId) {
        return collabQuestMemberPersistenceRepository.findBySessionIdOrderByIdAsc(sessionId)
                .stream().map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CollabQuestMember> findByUserId(Long userId) {
        return collabQuestMemberPersistenceRepository.findByUserIdOrderByIdDesc(userId)
                .stream().map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CollabQuestMember> findByUserIdAndStatus(
            Long userId, CollabMemberStatus status) {
        return collabQuestMemberPersistenceRepository.findByUserIdAndStatusOrderByIdDesc(
                        userId, status)
                .stream().map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CollabQuestMember> findBySessionIdAndStatusIn(
            Long sessionId,
            List<CollabMemberStatus> statuses
    ) {
        return collabQuestMemberPersistenceRepository
                .findBySessionIdAndStatusIn(sessionId, statuses)
                .stream()
                .map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CollabQuestMember> findByUserIdAndQuestId(Long userId, Long questId) {
        return collabQuestMemberPersistenceRepository.findByUserIdAndQuestId(userId, questId)
                .stream()
                .map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CollabQuestMember> findByUserIdAndQuestIdAndSessionStatusIn(
            Long userId,
            Long questId,
            List<CollabQuestStatus> sessionStatuses
    ) {
        return collabQuestMemberPersistenceRepository
                .findByUserIdAndQuestIdAndSessionStatusIn(userId, questId, sessionStatuses)
                .stream()
                .map(CollabQuestMemberPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
