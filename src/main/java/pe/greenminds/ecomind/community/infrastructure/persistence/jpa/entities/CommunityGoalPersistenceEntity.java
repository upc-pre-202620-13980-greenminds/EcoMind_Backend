package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters.CommunityGoalTopicPersistenceConverter;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters.CommunityGoalStatusPersistenceConverter;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity @Table(name="community_goals")
public class CommunityGoalPersistenceEntity extends AuditableAbstractPersistenceEntity{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;@Column(nullable=false)

    private Long communityId;

    @Convert(converter = CommunityGoalTopicPersistenceConverter.class)
    @Column(nullable=false)
    private CommunityGoalTopic topic;

    @Column(nullable=false)
    private Integer target;

    @Column(nullable=false)
    private Integer progress;

    @Column(nullable=false)
    private Integer participants;

    @Convert(converter = CommunityGoalStatusPersistenceConverter.class)
    @Column(nullable=false)
    private CommunityGoalStatus status;

    protected CommunityGoalPersistenceEntity(){}

    public CommunityGoalPersistenceEntity(Long c, CommunityGoalTopic topic, Integer t){this(c,topic,t,0,0,CommunityGoalStatus.ACTIVE);}

    public CommunityGoalPersistenceEntity(Long c, CommunityGoalTopic topic, Integer t,Integer p,Integer n,CommunityGoalStatus s){
        communityId=c;
        this.topic=topic;
        target=t;
        progress=p;
        participants=n;
        status=s;
    }

    public Long getId(){
        return id;
    }

    public Long getCommunityId(){
        return communityId;
    }

    public CommunityGoalTopic getTopic(){
        return topic;
    }

    public Integer getTarget(){
        return target;
    }

    public Integer getProgress(){
        return progress;
    }

    public Integer getParticipants(){
        return participants;
    }

    public CommunityGoalStatus getStatus(){
        return status;
    }

    public void applyProgress(Integer p,Integer n){
        progress=Math.min(target,p);
        participants=n;
        status=progress>=target?CommunityGoalStatus.COMPLETED:CommunityGoalStatus.ACTIVE;
    }

    public void increment(){
        applyProgress(progress+1,participants+1);
    }
}
