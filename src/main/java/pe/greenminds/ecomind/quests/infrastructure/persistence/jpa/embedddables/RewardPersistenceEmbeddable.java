package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.embedddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RewardPersistenceEmbeddable {
    @Column(name = "gem_reward")
    private Integer gemReward;
    @Column(name = "ecopoint_reward")
    private Integer ecopointReward;

    public RewardPersistenceEmbeddable() {}

    public RewardPersistenceEmbeddable(Integer gemReward, Integer ecopointReward) {
        this.gemReward = gemReward;
        this.ecopointReward = ecopointReward;
    }

    public Integer getGemReward() {return gemReward;}
    public void setGemReward(Integer gemReward) {this.gemReward = gemReward;}

    public Integer getEcopointReward() {return ecopointReward;}

    public void setEcopointReward(Integer ecopointReward) {
        this.ecopointReward = ecopointReward;
    }
}
