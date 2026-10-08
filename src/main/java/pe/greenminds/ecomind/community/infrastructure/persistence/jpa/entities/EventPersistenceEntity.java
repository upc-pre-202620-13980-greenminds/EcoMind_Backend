package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity @Table(name="community_events")
public class EventPersistenceEntity extends AuditableAbstractPersistenceEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private Long communityId;
  @Column(nullable=false) private Long authorId;
  @Column(nullable=false) private String name;
  @Column(length=3000) private String description;
  @Column(nullable=false) private LocalDate date;
  @Column(nullable=false) private LocalTime startTime;
  private String location; private Double latitude; private Double longitude;
  @Column(nullable=false) private Integer capacity; private String imageUrl;
  protected EventPersistenceEntity(){}
  public EventPersistenceEntity(Long id,Long communityId,Long authorId,String name,String description,LocalDate date,LocalTime startTime,String location,Double latitude,Double longitude,Integer capacity,String imageUrl){this.id=id;this.communityId=communityId;this.authorId=authorId;this.name=name;this.description=description;this.date=date;this.startTime=startTime;this.location=location;this.latitude=latitude;this.longitude=longitude;this.capacity=capacity;this.imageUrl=imageUrl;}
  public Long getId(){return id;} public Long getCommunityId(){return communityId;} public Long getAuthorId(){return authorId;} public String getName(){return name;} public String getDescription(){return description;} public LocalDate getDate(){return date;} public LocalTime getStartTime(){return startTime;} public String getLocation(){return location;} public Double getLatitude(){return latitude;} public Double getLongitude(){return longitude;} public Integer getCapacity(){return capacity;} public String getImageUrl(){return imageUrl;}
}
