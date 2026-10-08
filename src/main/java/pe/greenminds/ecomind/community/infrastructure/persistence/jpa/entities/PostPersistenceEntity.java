package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "community_posts")
public class PostPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long communityId;
    private Long authorId;
    @Column(nullable = false, length = 3000)
    private String content;
    @Column(nullable = false)
    private String postType;
    private String imageUrl;
    private Long relatedEventId;

    protected PostPersistenceEntity() {
    }

    public PostPersistenceEntity(Long communityId, Long authorId, String content, String postType, String imageUrl,
            Long eventId) {
        this.communityId = communityId;
        this.authorId = authorId;
        this.content = content;
        this.postType = postType;
        this.imageUrl = imageUrl;
        this.relatedEventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public String getPostType() {
        return postType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getRelatedEventId() {
        return relatedEventId;
    }
}
