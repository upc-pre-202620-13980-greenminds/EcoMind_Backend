package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*; import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
@Entity @Table(name="community_post_reactions",uniqueConstraints={@UniqueConstraint(columnNames={"post_id","user_id"})})
public class PostReactionPersistenceEntity extends AuditableAbstractPersistenceEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private Long postId; @Column(nullable=false) private Long userId; @Column(nullable=false) private String reactionType;
 protected PostReactionPersistenceEntity(){} public PostReactionPersistenceEntity(Long postId,Long userId,String type){this.postId=postId;this.userId=userId;this.reactionType=type;}
 public Long getId(){return id;} public Long getPostId(){return postId;} public Long getUserId(){return userId;} public String getReactionType(){return reactionType;} public void changeType(String type){reactionType=type;}
}
