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

    public CommunityGoalPersistenceEntity(Long communityId, CommunityGoalTopic topic, Integer target) {
        this(communityId, topic, target, 0, 0, CommunityGoalStatus.ACTIVE);
    }

    public CommunityGoalPersistenceEntity(Long communityId, CommunityGoalTopic topic, Integer target,
            Integer progress, Integer participants, CommunityGoalStatus status) {
        this.communityId = communityId;
        this.topic = topic;
        this.target = target;
        this.progress = progress;
        this.participants = participants;
        this.status = status;
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

    public void applyProgress(Integer progress, Integer participants) {
        this.progress = Math.min(target, progress);
        this.participants = participants;
        status = this.progress >= target ? CommunityGoalStatus.COMPLETED : CommunityGoalStatus.ACTIVE;
    }

    public void increment(){
        applyProgress(progress+1,participants+1);
    }
}
