package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.QuestPersistenceEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface QuestPersistenceRepository extends JpaRepository<QuestPersistenceEntity, Long> {
    boolean existsByMinigameId(Long minigameId);
    @Query("""
            SELECT q FROM QuestPersistenceEntity q
            WHERE q.publicationStatus = :status
               OR (q.publicationStatus IS NULL
                   AND :status = pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus.PUBLISHED)
            """)
    List<QuestPersistenceEntity> findByPublicationStatus(@Param("status") QuestPublicationStatus status);

    @Query("""
            SELECT q FROM QuestPersistenceEntity q
            WHERE q.versionGroupId = :versionGroupId
               OR (q.versionGroupId IS NULL AND q.id = :versionGroupId)
            ORDER BY q.versionNumber DESC, q.id DESC
            """)
    List<QuestPersistenceEntity> findVersions(@Param("versionGroupId") Long versionGroupId);

    @Query("""
            SELECT q FROM QuestPersistenceEntity q
            WHERE (q.versionGroupId = :versionGroupId
                   OR (q.versionGroupId IS NULL AND q.id = :versionGroupId))
              AND (q.publicationStatus = pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus.PUBLISHED
                   OR q.publicationStatus IS NULL)
            """)
    Optional<QuestPersistenceEntity> findPublishedByVersionGroupId(
            @Param("versionGroupId") Long versionGroupId);

    Optional<QuestPersistenceEntity> findByQuestTypeAndAssignedDateAndPublicationStatus(
            QuestType questType,
            LocalDate assignedDate,
            QuestPublicationStatus publicationStatus
    );

    @Query("""
    SELECT q FROM QuestPersistenceEntity q
    WHERE q.questType = :questType
      AND q.publicationStatus = pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus.PUBLISHED
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
      AND q.publicationStatus = pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus.PUBLISHED
    """)
    List<QuestPersistenceEntity> search(
            @Param("title") String title,
            @Param("category") Category category,
            @Param("questType") QuestType questType,
            @Param("age") Integer age,
            @Param("theme") Theme theme
    );
}
