package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.AchievementAwardPersistenceEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AchievementAwardRepositoryImpl implements AchievementAwardRepository {
    private final EntityManager entities;
    private final JdbcTemplate jdbc;

    public AchievementAwardRepositoryImpl(EntityManager entities, JdbcTemplate jdbc) {
        this.entities = entities;
        this.jdbc = jdbc;
    }

    public boolean addIfAbsent(AchievementAward award) {
        // Unique constraint preserves the first award, including source event and date, on retries.
        // Award uniqueness is enforced by the database; different beneficiaries do not share a
        // lock.
        return jdbc.update(
                        """
                        INSERT INTO achievement_awards
                          (id, achievement_id, scope, beneficiary_id, source_event_id, awarded_at, community_id, user_progress_id, family_score_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT DO NOTHING
                        """,
                        award.id().toString(),
                        award.achievementId().toString(),
                        award.scope().name(),
                        award.beneficiaryId(),
                        award.sourceEventId().toString(),
                        Timestamp.from(award.awardedAt()),
                        award.communityId() == null ? null : award.communityId().toString(),
                        award.scope() == AchievementScope.INDIVIDUAL ? award.beneficiaryId() : null,
                        award.scope() == AchievementScope.FAMILY ? award.beneficiaryId() : null)
                == 1;
    }

    public Optional<AchievementAward> findById(UUID id) {
        return Optional.ofNullable(
                        entities.find(AchievementAwardPersistenceEntity.class, id.toString()))
                .map(this::map);
    }

    public List<AchievementAward> findByCommunity(Long community, int page, int size) {
        return entities
                .createQuery(
                        "from AchievementAwardPersistenceEntity where scope='COMMUNITY' and"
                                + " communityId=:community order by awardedAt desc,id",
                        AchievementAwardPersistenceEntity.class)
                .setParameter("community", community.toString())
                .setFirstResult(Math.multiplyExact(page, size))
                .setMaxResults(size)
                .getResultList()
                .stream()
                .map(this::map)
                .toList();
    }

    private AchievementAward map(AchievementAwardPersistenceEntity row) {
        return new AchievementAward(
                UUID.fromString(row.getId()),
                UUID.fromString(row.getAchievementId()),
                AchievementScope.valueOf(row.getScope()),
                row.getBeneficiaryId(),
                UUID.fromString(row.getSourceEventId()),
                row.getAwardedAt(),
                row.getCommunityId() == null ? null : Long.valueOf(row.getCommunityId()));
    }

    public List<AchievementAward> findByBeneficiary(
            AchievementScope scope, Long beneficiaryId, int page, int size) {
        return entities
                .createQuery(
                        "from AchievementAwardPersistenceEntity where scope = :scope and"
                                + " beneficiaryId = :beneficiary order by awardedAt desc, id",
                        AchievementAwardPersistenceEntity.class)
                .setParameter("scope", scope.name())
                .setParameter("beneficiary", beneficiaryId)
                .setFirstResult(Math.multiplyExact(page, size))
                .setMaxResults(size)
                .getResultList()
                .stream()
                .map(
                        row ->
                                new AchievementAward(
                                        UUID.fromString(row.getId()),
                                        UUID.fromString(row.getAchievementId()),
                                        AchievementScope.valueOf(row.getScope()),
                                        row.getBeneficiaryId(),
                                        UUID.fromString(row.getSourceEventId()),
                                        row.getAwardedAt(),
                                        row.getCommunityId() == null
                                                ? null
                                                : Long.valueOf(row.getCommunityId())))
                .toList();
    }
}
