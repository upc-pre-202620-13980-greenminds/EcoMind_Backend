package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.ActivityQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivitiesByQuestIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityByIdQuery;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityQueryServiceImpl implements ActivityQueryService {
    private final ActivityRepository activityRepository;

    public ActivityQueryServiceImpl(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public Optional<Activity> handle(GetActivityByIdQuery query){
        return activityRepository.findById(query.id());
    }

    @Override
    public List<Activity> handle(GetActivitiesByQuestIdQuery query){
        return activityRepository.findByQuestsIdOrderByOrderAsc(query.questId());
    }
}
