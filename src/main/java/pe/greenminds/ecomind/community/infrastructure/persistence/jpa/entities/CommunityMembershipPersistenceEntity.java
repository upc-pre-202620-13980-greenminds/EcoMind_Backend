package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name="community_memberships", uniqueConstraints={@UniqueConstraint(columnNames={"community_id","user_id"})})
public class CommunityMembershipPersistenceEntity extends AuditableAbstractPersistenceEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private Long communityId;
  @Column(nullable=false) private Long userId;
  @Column(nullable=false) private String role;
  protected CommunityMembershipPersistenceEntity() {}
  public CommunityMembershipPersistenceEntity(Long communityId,Long userId,String role){this.communityId=communityId;this.userId=userId;this.role=role;}
  public Long getId(){return id;} public Long getCommunityId(){return communityId;} public Long getUserId(){return userId;} public String getRole(){return role;}
}
