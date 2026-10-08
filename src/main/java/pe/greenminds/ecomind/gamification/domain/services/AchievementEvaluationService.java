package pe.greenminds.ecomind.gamification.domain.services;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;

public class AchievementEvaluationService {
    public boolean qualifies(Achievement definition, long confirmedValue) {
        return definition.isSatisfiedBy(confirmedValue);
    }
}
