package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.ActivityUserQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUserByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUserByQuestUserIdAndActivityIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActivityUsersByQuestUserIdQuery;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityUserQueryServiceImpl implements ActivityUserQueryService {
    private final ActivityUserRepository activityUserRepository;

    public ActivityUserQueryServiceImpl(ActivityUserRepository activityUserRepository) {
        this.activityUserRepository = activityUserRepository;
    }

    @Override
    public Optional<ActivityUser> handle(GetActivityUserByIdQuery query) {
        return activityUserRepository.findById(query.id());
    }

    @Override
    public Optional<ActivityUser> handle(
            GetActivityUserByQuestUserIdAndActivityIdQuery query
    ) {
        return activityUserRepository.findByQuestUserIdAndActivityId(
                query.questUserId(),
                query.activityId()
        );
    }

    @Override
    public List<ActivityUser> handle(GetActivityUsersByQuestUserIdQuery query) {
        return activityUserRepository.findByQuestUserId(query.questUserId());
    }
}
