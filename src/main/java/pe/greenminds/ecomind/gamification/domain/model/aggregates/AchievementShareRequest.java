package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementShareStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AchievementShareRequest(
        UUID id,
        UUID awardId,
        Long requestedBy,
        Long communityId,
        AchievementShareStatus status,
        Long publicationId,
        Instant createdAt,
        Instant confirmedAt) {
    public AchievementShareRequest(
            UUID id,
            UUID awardId,
            Long requestedBy,
            Long communityId,
            AchievementShareStatus status,
            Long publicationId,
            Instant createdAt) {
        this(
                id,
                awardId,
                requestedBy,
                communityId,
                status,
                publicationId,
                createdAt,
                status == AchievementShareStatus.PUBLISHED ? createdAt : null);
    }

    public AchievementShareRequest {
        Objects.requireNonNull(id);
        Objects.requireNonNull(awardId);
        Objects.requireNonNull(communityId);
        Objects.requireNonNull(status);
        Objects.requireNonNull(createdAt);
        if (communityId <= 0
                || (publicationId != null && publicationId <= 0)
                || requestedBy == null
                || requestedBy <= 0
                || (status == AchievementShareStatus.PUBLISHED) != (publicationId != null))
            throw new IllegalArgumentException("Invalid share request");
        if ((status == AchievementShareStatus.PUBLISHED) != (confirmedAt != null)
                || (confirmedAt != null && confirmedAt.isBefore(createdAt)))
            throw new IllegalArgumentException("Invalid publication confirmation time");
    }

    public AchievementShareRequest confirm(
            UUID award, Long user, Long community, Long publication, Instant confirmedAt) {
        if (!awardId.equals(award) || !requestedBy.equals(user) || !communityId.equals(community))
            throw new IllegalArgumentException("Publication does not match its request");
        Objects.requireNonNull(publication);
        if (status == AchievementShareStatus.PUBLISHED && !publicationId.equals(publication))
            throw new IllegalStateException("Request already confirmed with another publication");
        if (status == AchievementShareStatus.PUBLISHED) return this;
        return new AchievementShareRequest(
                id,
                awardId,
                requestedBy,
                communityId,
                AchievementShareStatus.PUBLISHED,
                publication,
                createdAt,
                confirmedAt);
    }
}
