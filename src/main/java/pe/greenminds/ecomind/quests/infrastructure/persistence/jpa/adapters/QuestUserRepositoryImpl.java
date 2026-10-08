package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.QuestUserPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.QuestUserPersistenceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestUserRepositoryImpl implements QuestUserRepository {
    private final QuestUserPersistenceRepository questUserPersistenceRepository;

    public QuestUserRepositoryImpl(
            QuestUserPersistenceRepository questUserPersistenceRepository
    ) {
        this.questUserPersistenceRepository = questUserPersistenceRepository;
    }

    @Override
    public QuestUser save(QuestUser questUser) {
        var savedEntity = questUserPersistenceRepository.save(
                QuestUserPersistenceAssembler.toPersistenceFromDomain(questUser)
        );
        return QuestUserPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<QuestUser> findById(Long id) {
        return questUserPersistenceRepository.findById(id)
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<QuestUser> findByUserIdAndQuestId(Long userId, Long questId) {
        return questUserPersistenceRepository
                .findFirstByUserIdAndQuestIdOrderByIdDesc(userId, questId)
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<QuestUser> findFirstByUserIdAndQuestId(Long userId, Long questId) {
        return questUserPersistenceRepository
                .findFirstByUserIdAndQuestIdOrderByIdAsc(userId, questId)
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<QuestUser> findFirstByUserIdAndQuestIdAndStatusIn(
            Long userId,
            Long questId,
            List<QuestStatus> statuses
    ) {
        return questUserPersistenceRepository
                .findFirstByUserIdAndQuestIdAndStatusInOrderByIdDesc(
                        userId,
                        questId,
                        statuses
                )
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<QuestUser> findByQuestId(Long questId) {
        return questUserPersistenceRepository.findByQuestId(questId)
                .stream()
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<QuestUser> findByQuestIdAndStatusIn(
            Long questId,
            List<QuestStatus> statuses
    ) {
        return questUserPersistenceRepository.findByQuestIdAndStatusIn(questId, statuses)
                .stream()
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<QuestUser> findDailyQuestUsersBeforeDateAndStatusIn(
            QuestType questType,
            LocalDate assignedDate,
            List<QuestStatus> statuses
    ) {
        return questUserPersistenceRepository
                .findDailyQuestUsersBeforeDateAndStatusIn(questType, assignedDate, statuses)
                .stream()
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<QuestUser> findDailyQuestUsersByUserIdBeforeDateAndStatusIn(
            Long userId,
            QuestType questType,
            LocalDate assignedDate,
            List<QuestStatus> statuses
    ) {
        return questUserPersistenceRepository
                .findDailyQuestUsersByUserIdBeforeDateAndStatusIn(userId, questType, assignedDate, statuses)
                .stream()
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public int countByUserIdsAndStatusAndQuestTypes(
            List<Long> userIds,
            QuestStatus status,
            List<QuestType> questTypes
    ) {
        if (userIds == null || userIds.isEmpty() || questTypes == null || questTypes.isEmpty()) {
            return 0;
        }
        return (int) questUserPersistenceRepository.countByUserIdsAndStatusAndQuestTypes(
                userIds,
                status,
                questTypes
        );
    }

    @Override
    public boolean existsByUserIdAndQuestId(Long userId, Long questId) {
        return questUserPersistenceRepository.existsByUserIdAndQuestId(userId, questId);
    }

    @Override
    public boolean existsByUserIdAndQuestIdAndStatusIn(
            Long userId,
            Long questId,
            List<QuestStatus> statuses
    ) {
        return questUserPersistenceRepository.existsByUserIdAndQuestIdAndStatusIn(
                userId,
                questId,
                statuses
        );
    }

    @Override
    public boolean existsByUserIdAndQuestIdAndStatusAndIdNot(
            Long userId,
            Long questId,
            QuestStatus status,
            Long excludedQuestUserId
    ) {
        return questUserPersistenceRepository.existsByUserIdAndQuestIdAndStatusAndIdNot(
                userId,
                questId,
                status,
                excludedQuestUserId
        );
    }

    @Override
    public void deleteById(Long id) {
        questUserPersistenceRepository.deleteById(id);
    }

    @Override
    public void deleteByQuestId(Long questId) {
        questUserPersistenceRepository.deleteByQuestId(questId);
    }

    @Override
    public List<QuestUser> findByUserIdAndStatus(Long userId, QuestStatus questStatus) {
        return questUserPersistenceRepository.findByUserIdAndStatus(userId, questStatus)
                .stream()
                .map(QuestUserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
