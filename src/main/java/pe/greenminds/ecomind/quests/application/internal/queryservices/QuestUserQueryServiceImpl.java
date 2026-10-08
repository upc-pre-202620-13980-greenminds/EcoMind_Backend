package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.internal.services.DailyQuestLifecycleService;
import pe.greenminds.ecomind.quests.application.queryservices.QuestUserQueryService;
import pe.greenminds.ecomind.quests.application.queryservices.QuestUserVersionStatus;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserByUserIdAndQuestIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUsersByUserIdAndStatusQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestUserVersionStatusQuery;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestUserQueryServiceImpl implements QuestUserQueryService {
    private final QuestUserRepository questUserRepository;
    private final ActivityRepository activityRepository;
    private final ActivityUserRepository activityUserRepository;
    private final DailyQuestLifecycleService dailyQuestLifecycleService;

    public QuestUserQueryServiceImpl(
            QuestUserRepository questUserRepository,
            ActivityRepository activityRepository,
            ActivityUserRepository activityUserRepository,
            DailyQuestLifecycleService dailyQuestLifecycleService
    ) {
        this.questUserRepository = questUserRepository;
        this.activityRepository = activityRepository;
        this.activityUserRepository = activityUserRepository;
        this.dailyQuestLifecycleService = dailyQuestLifecycleService;
    }

    @Override
    public Optional<QuestUser> handle(GetQuestUserByIdQuery query) {
        var questUser = questUserRepository.findById(query.id());
        questUser.ifPresent(value -> dailyQuestLifecycleService.expireOpenDailyQuestsForUser(value.getUserId()));
        return questUserRepository.findById(query.id());
    }

    @Override
    public Optional<QuestUser> handle(GetQuestUserByUserIdAndQuestIdQuery query) {
        dailyQuestLifecycleService.expireOpenDailyQuestsForUser(query.userId());
        return questUserRepository.findByUserIdAndQuestId(query.userId(), query.questId());
    }

    @Override
    public List<QuestUser> handle(GetQuestUsersByUserIdAndStatusQuery query) {
        if (query.status() == pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus.IN_PROGRESS) {
            dailyQuestLifecycleService.ensureTodayDailyQuestForUser(query.userId());
        } else {
            dailyQuestLifecycleService.expireOpenDailyQuestsForUser(query.userId());
        }
        return questUserRepository.findByUserIdAndStatus(query.userId(), query.status());
    }

    @Override
    public Optional<QuestUserVersionStatus> handle(GetQuestUserVersionStatusQuery query) {
        var questUser = questUserRepository.findById(query.questUserId());
        if (questUser.isEmpty()) {
            return Optional.empty();
        }
        dailyQuestLifecycleService.expireOpenDailyQuestsForUser(questUser.get().getUserId());
        questUser = questUserRepository.findById(query.questUserId());
        if (questUser.isEmpty()) {
            return Optional.empty();
        }

        var currentActivities = activityRepository.findByQuestsIdOrderByOrderAsc(
                questUser.get().getQuestId()
        );
        Map<Long, ActivityUser> assignedActivities = activityUserRepository
                .findByQuestUserId(query.questUserId())
                .stream()
                .collect(Collectors.toMap(ActivityUser::getActivityId, Function.identity()));

        var missingActivityIds = new ArrayList<Long>();
        var outdatedActivityIds = new ArrayList<Long>();

        for (var activity : currentActivities) {
            var activityUser = assignedActivities.get(activity.getId());
            if (activityUser == null) {
                missingActivityIds.add(activity.getId());
                continue;
            }

            if (!Objects.equals(
                    activity.getDescription(),
                    activityUser.getActivityDescription()
            ) || !Objects.equals(
                    activity.getActivityConfiguration(),
                    activityUser.getActivityConfiguration()
            )) {
                outdatedActivityIds.add(activity.getId());
            }
        }

        return Optional.of(
                new QuestUserVersionStatus(
                        query.questUserId(),
                        missingActivityIds.isEmpty() && outdatedActivityIds.isEmpty(),
                        List.copyOf(missingActivityIds),
                        List.copyOf(outdatedActivityIds)
                )
        );
    }
}
