package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.Instant;

public record Post(
        Long id,
        Long communityId,
        Long authorId,
        String content,
        String postType,
        String imageUrl,
        Long relatedEventId,
        Instant createdAt) {

    public Post {
        if (communityId == null || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Community and post content are required");
        }
    }

    public Post(
            Long id,
            Long communityId,
            Long authorId,
            String content,
            String postType,
            String imageUrl,
            Long relatedEventId) {
        this(id, communityId, authorId, content, postType, imageUrl, relatedEventId, null);
    }
}