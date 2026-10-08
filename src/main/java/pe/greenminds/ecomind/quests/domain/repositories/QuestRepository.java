package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

public interface QuestRepository {
    Optional<Quest> findById(Long id);
    List<Quest> findAll();
    Optional<Quest> findByTypeAndAssignedDate(QuestType questType, LocalDate assignedDate);
    Optional<Quest> findLatestTemplateByType(QuestType questType);
    List<Quest> search(
            String title,
            Category category,
            QuestType questType,
            Integer minimumAge,
            Theme type
    );
    Quest save(Quest quest);
    void deleteById(Long id);

    boolean existsById(Long id);
    boolean existsByMinigameId(Long minigameId);
}
