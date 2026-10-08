package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*; import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity @Table(name="community_event_registrations",uniqueConstraints={@UniqueConstraint(columnNames={"event_id","user_id"})})
public class EventRegistrationPersistenceEntity extends AuditableAbstractPersistenceEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private Long eventId; @Column(nullable=false) private Long userId;
  @Column(nullable=false) private String registrationType; private Long familyId;
  @Column(nullable=false) private Integer participantCount; @Column(nullable=false) private String status;
  protected EventRegistrationPersistenceEntity(){}
  public EventRegistrationPersistenceEntity(Long eventId,Long userId,String type,Long familyId,Integer count){this.eventId=eventId;this.userId=userId;this.registrationType=type;this.familyId=familyId;this.participantCount=count;this.status="REGISTERED";}
  public Long getId(){return id;} public Long getEventId(){return eventId;} public Long getUserId(){return userId;} public String getRegistrationType(){return registrationType;} public Long getFamilyId(){return familyId;} public Integer getParticipantCount(){return participantCount;} public String getStatus(){return status;} public void cancel(){status="CANCELLED";}
}
