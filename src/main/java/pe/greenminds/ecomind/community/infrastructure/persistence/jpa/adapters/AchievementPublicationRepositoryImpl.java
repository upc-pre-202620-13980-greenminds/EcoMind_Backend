package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementNotice;
import pe.greenminds.ecomind.community.domain.model.aggregates.AchievementPost;
import pe.greenminds.ecomind.community.domain.repositories.AchievementPublicationRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.AchievementPostPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.AchievementNoticePersistenceEntity;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.AchievementPostPersistenceEntity;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AchievementPublicationRepositoryImpl implements AchievementPublicationRepository {
    private final EntityManager entities;
    private final JdbcTemplate jdbc;

    public AchievementPublicationRepositoryImpl(EntityManager entities, JdbcTemplate jdbc) {
        this.entities = entities;
        this.jdbc = jdbc;
    }

    public void lockCommunity(Long id) {
        if (entities.find(CommunityPersistenceEntity.class, id, LockModeType.PESSIMISTIC_WRITE)
                == null) throw new IllegalArgumentException("Community does not exist");
    }

    public Optional<AchievementPost> findByRequestId(UUID requestId) {
        return entities.createQuery(
                        "from AchievementPostPersistenceEntity where requestId=:request",
                        AchievementPostPersistenceEntity.class)
                .setParameter("request", requestId.toString())
                .getResultStream()
                .findFirst()
                .map(AchievementPostPersistenceAssembler::toDomain);
    }

    public AchievementPost save(AchievementPost post) {
        var row =
                AchievementPostPersistenceAssembler.toEntity(
                        post,
                        entities.getReference(
                                CommunityPersistenceEntity.class, post.communityId()));
        entities.persist(row);
        entities.flush();
        return AchievementPostPersistenceAssembler.toDomain(row);
    }

    public AchievementNotice recordNotice(AchievementNotice notice) {
        jdbc.update(
                "INSERT INTO community_achievement_notices"
                    + " (award_id,achievement_id,user_id,occurred_at) VALUES (?,?,?,?) ON CONFLICT"
                    + " DO NOTHING",
                notice.awardId().toString(),
                notice.achievementId().toString(),
                notice.userId(),
                Timestamp.from(notice.occurredAt()));
        var row =
                entities.find(
                        AchievementNoticePersistenceEntity.class, notice.awardId().toString());
        return new AchievementNotice(
                UUID.fromString(row.getAwardId()),
                UUID.fromString(row.getAchievementId()),
                row.getUserId(),
                row.getOccurredAt());
    }

    public List<AchievementPost> findPosts(
            Long communityId, Long authorId, UUID awardId, int page, int size) {
        return entities
                .createQuery(
                        "from AchievementPostPersistenceEntity where community.id=:community and"
                            + " (:author is null or authorId=:author) and (:award is null or"
                            + " awardId=:award) order by publishedAt desc,id desc",
                        AchievementPostPersistenceEntity.class)
                .setParameter("community", communityId)
                .setParameter("author", authorId)
                .setParameter("award", awardId == null ? null : awardId.toString())
                .setFirstResult(Math.multiplyExact(page, size))
                .setMaxResults(size)
                .getResultList()
                .stream()
                .map(AchievementPostPersistenceAssembler::toDomain)
                .toList();
    }
}
