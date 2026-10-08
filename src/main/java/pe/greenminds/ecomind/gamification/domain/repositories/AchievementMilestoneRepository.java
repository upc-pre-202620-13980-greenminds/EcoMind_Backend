package pe.greenminds.ecomind.gamification.domain.repositories;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;

import java.util.UUID;

public interface AchievementMilestoneRepository {
    void lock(String beneficiary);

    void record(AchievementMetric metric, String beneficiary, UUID execution);

    long count(AchievementMetric metric, String beneficiary);
}
