package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters.CommunityTypePersistenceConverter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name="community_communities")
public class CommunityPersistenceEntity extends AuditableAbstractPersistenceEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String name;
  @Column(length=2000) private String description;
  @Convert(converter = CommunityTypePersistenceConverter.class)
  @Column(nullable=false) private CommunityType type;
  private String topic;
  private String locality;
  private Integer memberLimit;
  private String iconUrl;
  @Column(nullable=false) private Long createdBy;
  protected CommunityPersistenceEntity() {}
  public CommunityPersistenceEntity(Long id,String name,String description,CommunityType type,String topic,String locality,Integer memberLimit,String iconUrl,Long createdBy){this.id=id;this.name=name;this.description=description;this.type=type;this.topic=topic;this.locality=locality;this.memberLimit=memberLimit;this.iconUrl=iconUrl;this.createdBy=createdBy;}
  public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public CommunityType getType(){return type;} public String getTopic(){return topic;} public String getLocality(){return locality;} public Integer getMemberLimit(){return memberLimit;} public String getIconUrl(){return iconUrl;} public Long getCreatedBy(){return createdBy;}
}
