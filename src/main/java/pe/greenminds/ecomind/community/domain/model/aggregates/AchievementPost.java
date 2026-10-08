package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A voluntary achievement publication; notices alone never create this aggregate. */
public record AchievementPost(
        Long id,
        UUID requestId,
        UUID awardId,
        Long authorId,
        Long communityId,
        Instant publishedAt) {
    public AchievementPost {
        Objects.requireNonNull(requestId);
        Objects.requireNonNull(awardId);
        Objects.requireNonNull(publishedAt);
        if (authorId == null || authorId <= 0 || communityId == null || communityId <= 0)
            throw new IllegalArgumentException("Author and community must be positive");
    }

    public void requireSameRequest(UUID award, Long author, Long community) {
        if (!awardId.equals(award) || !authorId.equals(author) || !communityId.equals(community))
            throw new IllegalArgumentException(
                    "Publication request id was reused with different data");
    }
}
