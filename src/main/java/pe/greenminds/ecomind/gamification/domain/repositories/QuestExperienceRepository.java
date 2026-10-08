package pe.greenminds.ecomind.gamification.domain.repositories;

import java.util.OptionalLong;

public interface QuestExperienceRepository {
    OptionalLong find(Long questId);

    void configure(Long questId, long experience);
}
