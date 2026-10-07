package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.internal.services.DailyQuestLifecycleService;
import pe.greenminds.ecomind.quests.application.queryservices.QuestQueryService;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.queries.GetAllQuestsQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetQuestByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.SearchQuestQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;

import java.util.List;
import java.util.Comparator;
import java.util.Optional;

@Service
public class QuestQueryServiceImpl implements QuestQueryService {

    private final QuestRepository questRepository;
    private final DailyQuestLifecycleService dailyQuestLifecycleService;

    public QuestQueryServiceImpl(
            QuestRepository questRepository,
            DailyQuestLifecycleService dailyQuestLifecycleService
    ) {
        this.questRepository = questRepository;
        this.dailyQuestLifecycleService = dailyQuestLifecycleService;
    }

    @Override
    public Optional<Quest> handle(GetQuestByIdQuery query) {
        return questRepository.findById(query.id());
    }

    @Override
    public List<Quest> handle(GetAllQuestsQuery query) {
        dailyQuestLifecycleService.ensureTodayDailyQuest();
        return questRepository.findAll()
                .stream()
                .filter(quest -> quest.getType() != QuestType.FAMILY)
                .sorted(questDateComparator())
                .toList();
    }

    @Override
    public List<Quest> handle(SearchQuestQuery query) {
        dailyQuestLifecycleService.ensureTodayDailyQuest();
        var quests = questRepository.search(
                query.title(),
                query.category(),
                query.questType(),
                query.age(),
                query.type()
        );
        if (query.questType() == null) {
            return quests.stream()
                    .filter(quest -> quest.getType() != QuestType.FAMILY)
                    .sorted(questDateComparator())
                    .toList();
        }
        return quests.stream()
                .sorted(questDateComparator())
                .toList();
    }

    private Comparator<Quest> questDateComparator() {
        return Comparator
                .comparing(
                        Quest::getAssignedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
                .thenComparing(
                        Quest::getId,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );
    }
}
