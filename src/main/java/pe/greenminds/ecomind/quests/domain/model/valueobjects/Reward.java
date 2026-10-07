package pe.greenminds.ecomind.quests.domain.model.valueobjects;

public record Reward(Integer gems, Integer ecopoints) {
    public Reward{
        if(ecopoints == null || gems == null){
            throw new IllegalArgumentException("invalid reward values");
        }
        if (ecopoints < 0 || gems < 0){
            throw new IllegalArgumentException("can't award negative rewards");
        }
    }
}
