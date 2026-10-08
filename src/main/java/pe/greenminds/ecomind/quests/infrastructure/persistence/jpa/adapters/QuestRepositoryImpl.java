package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.QuestPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.QuestPersistenceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestRepositoryImpl implements QuestRepository {
    private final QuestPersistenceRepository questPersistenceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public QuestRepositoryImpl(QuestPersistenceRepository questPersistenceRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.questPersistenceRepository = questPersistenceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<Quest> findById(Long id){
        return questPersistenceRepository.findById(id).map(QuestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Quest> search(
            String title,
            Category category,
            QuestType questType,
            Integer age,
            Theme type
    ) {
        String normalizedTitle = title == null ? "" : title.trim();

        return questPersistenceRepository
                .search(normalizedTitle, category, questType, age, type)
                .stream()
                .map(QuestPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Quest> findAll(){
        return questPersistenceRepository.findAll().stream().map(QuestPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public List<Quest> findByPublicationStatus(QuestPublicationStatus status) {
        return questPersistenceRepository.findByPublicationStatus(status).stream()
                .map(QuestPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public List<Quest> findByVersionGroupId(Long versionGroupId) {
        return questPersistenceRepository.findVersions(versionGroupId)
                .stream().map(QuestPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Optional<Quest> findPublishedByVersionGroupId(Long versionGroupId) {
        return questPersistenceRepository.findPublishedByVersionGroupId(versionGroupId)
                .map(QuestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Quest> findByTypeAndAssignedDate(QuestType questType, LocalDate assignedDate) {
        return questPersistenceRepository.findByQuestTypeAndAssignedDateAndPublicationStatus(
                        questType, assignedDate, QuestPublicationStatus.PUBLISHED)
                .map(QuestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Quest> findLatestTemplateByType(QuestType questType) {
        return questPersistenceRepository.findTemplatesByQuestType(
                        questType,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .map(QuestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Quest save(Quest quest) {
        boolean isNew = quest.getId() == null;
        var savedEntity = questPersistenceRepository.save(QuestPersistenceAssembler.toPersistenceFromDomain(quest));
        var savedQuest = QuestPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if(isNew){
            savedQuest.onCreated();
            savedQuest.domainEvents().forEach(applicationEventPublisher::publishEvent);
            savedQuest.clearDomainEvents();
        }
        return savedQuest;
    }

    @Override
    public boolean existsById(Long id) {
        return questPersistenceRepository.existsById(id);
    }

    @Override
    public boolean existsByMinigameId(Long minigameId) {
        return questPersistenceRepository.existsByMinigameId(minigameId);
    }
}
