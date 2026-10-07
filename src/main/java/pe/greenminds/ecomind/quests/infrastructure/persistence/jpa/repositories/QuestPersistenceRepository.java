package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.QuestPersistenceEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface QuestPersistenceRepository extends JpaRepository<QuestPersistenceEntity, Long> {
    boolean existsByMinigameId(Long minigameId);

    Optional<QuestPersistenceEntity> findByQuestTypeAndAssignedDate(
            QuestType questType,
            LocalDate assignedDate
    );

    @Query("""
    SELECT q FROM QuestPersistenceEntity q
    WHERE q.questType = :questType
    ORDER BY
      CASE WHEN q.assignedDate IS NULL THEN 1 ELSE 0 END,
      q.assignedDate DESC,
      q.id DESC
    """)
    List<QuestPersistenceEntity> findTemplatesByQuestType(
            @Param("questType") QuestType questType,
            Pageable pageable
    );

    @Query("""
    SELECT q FROM QuestPersistenceEntity q
    WHERE (:title = '' OR LOWER(q.title)
           LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:category IS NULL OR q.category = :category)
      AND (:questType IS NULL OR q.questType = :questType)
      AND (:theme IS NULL OR q.theme= :theme)
      AND (:age IS NULL OR q.age <= :age)
    """)
    List<QuestPersistenceEntity> search(
            @Param("title") String title,
            @Param("category") Category category,
            @Param("questType") QuestType questType,
            @Param("age") Integer age,
            @Param("theme") Theme theme
    );
}
