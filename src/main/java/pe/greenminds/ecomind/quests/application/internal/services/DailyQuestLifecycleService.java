package pe.greenminds.ecomind.quests.application.internal.services;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DailyQuestLifecycleService {
    public static final ZoneId DAILY_ZONE = ZoneId.of("America/Lima");

    private static final List<QuestStatus> EXPIRABLE_STATUSES = List.of(
            QuestStatus.IN_PROGRESS,
            QuestStatus.READY_TO_COMPLETE
    );

    private final QuestRepository questRepository;
    private final QuestUserRepository questUserRepository;
    private final ActivityRepository activityRepository;
    private final ActivityUserRepository activityUserRepository;

    public DailyQuestLifecycleService(
            QuestRepository questRepository,
            QuestUserRepository questUserRepository,
            ActivityRepository activityRepository,
            ActivityUserRepository activityUserRepository
    ) {
        this.questRepository = questRepository;
        this.questUserRepository = questUserRepository;
        this.activityRepository = activityRepository;
        this.activityUserRepository = activityUserRepository;
    }

    public LocalDate today() {
        return LocalDate.now(DAILY_ZONE);
    }

    @Transactional
    public synchronized Optional<Quest> ensureTodayDailyQuest() {
        var today = today();
        var existingQuest = questRepository.findByTypeAndAssignedDate(
                QuestType.DAILY_QUEST,
                today
        );
        if (existingQuest.isPresent()) {
            ensureActivitiesFromTemplate(existingQuest.get());
            return existingQuest;
        }

        var template = findLatestDailyQuestWithActivities(Optional.empty());
        if (template.isEmpty()) {
            return Optional.empty();
        }

        if (today.equals(template.get().getAssignedDate())) {
            return template;
        }

        var savedQuest = questRepository.save(cloneQuestForDate(template.get(), today));
        savedQuest.initializeVersionGroup(savedQuest.getId());
        savedQuest.publish();
        savedQuest = questRepository.save(savedQuest);
        cloneActivities(template.get().getId(), savedQuest.getId());
        return Optional.of(savedQuest);
    }

    @Transactional
    public void expireOpenDailyQuests() {
        expire(
                questUserRepository.findDailyQuestUsersBeforeDateAndStatusIn(
                        QuestType.DAILY_QUEST,
                        today(),
                        EXPIRABLE_STATUSES
                )
        );
    }

    @Transactional
    public void expireOpenDailyQuestsForUser(Long userId) {
        expire(
                questUserRepository.findDailyQuestUsersByUserIdBeforeDateAndStatusIn(
                        userId,
                        QuestType.DAILY_QUEST,
                        today(),
                        EXPIRABLE_STATUSES
                )
        );
    }

    public synchronized Optional<QuestUser> ensureTodayDailyQuestForUser(Long userId) {
        expireOpenDailyQuestsForUser(userId);

        var dailyQuest = ensureTodayDailyQuest();
        if (dailyQuest.isEmpty()) {
            return Optional.empty();
        }

        var existingQuestUser = questUserRepository.findFirstByUserIdAndQuestId(
                userId,
                dailyQuest.get().getId()
        );
        if (existingQuestUser.isPresent()) {
            return existingQuestUser;
        }

        var activities = activityRepository.findByQuestsIdOrderByOrderAsc(
                dailyQuest.get().getId()
        );
        if (activities.isEmpty()) {
            return Optional.empty();
        }

        QuestUser savedQuestUser;
        try {
            savedQuestUser = questUserRepository.save(
                    new QuestUser(userId, dailyQuest.get().getId(), null)
            );
        } catch (DataIntegrityViolationException ignored) {
            return questUserRepository.findFirstByUserIdAndQuestId(
                    userId,
                    dailyQuest.get().getId()
            );
        }

        for (var activity : activities) {
            activityUserRepository.save(
                    new ActivityUser(
                            savedQuestUser.getId(),
                            activity.getId(),
                            activity.getDescription(),
                            activity.getActivityConfiguration(),
                            null
                    )
            );
        }

        return Optional.of(savedQuestUser);
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")
    public void closePreviousDayDailyQuests() {
        ensureTodayDailyQuest();
        expireOpenDailyQuests();
    }

    private Quest cloneQuestForDate(Quest template, LocalDate assignedDate) {
        return new Quest(
                template.getMinigameId(),
                titleForDate(template.getTitle(), assignedDate),
                template.getCategory(),
                template.getDescription(),
                QuestType.DAILY_QUEST,
                template.getAge(),
                template.getRewardValue(),
                template.getTime(),
                template.getImage(),
                template.getTheme(),
                assignedDate
        );
    }

    private String titleForDate(String title, LocalDate assignedDate) {
        var baseTitle = title == null ? "Daily Quest" : title.replaceFirst(" - \\d{4}-\\d{2}-\\d{2}$", "");
        return "%s - %s".formatted(baseTitle, assignedDate);
    }

    private void cloneActivities(Long sourceQuestId, Long targetQuestId) {
        var activities = activityRepository.findByQuestsIdOrderByOrderAsc(sourceQuestId);
        for (var activity : activities) {
            activityRepository.save(
                    new Activity(
                            targetQuestId,
                            activity.getDescription(),
                            activity.getOrder(),
                            activity.getActivityType(),
                            activity.getActivityConfiguration(),
                            activity.getImage()
                    )
            );
        }
    }

    private void ensureActivitiesFromTemplate(Quest quest) {
        if (!activityRepository.findByQuestsIdOrderByOrderAsc(quest.getId()).isEmpty()) {
            return;
        }

        findLatestDailyQuestWithActivities(Optional.of(quest.getId()))
                .ifPresent(template -> cloneActivities(template.getId(), quest.getId()));
    }

    private Optional<Quest> findLatestDailyQuestWithActivities(Optional<Long> excludedQuestId) {
        return questRepository.findByPublicationStatus(
                        pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus.PUBLISHED)
                .stream()
                .filter(quest -> quest.getType() == QuestType.DAILY_QUEST)
                .filter(quest -> excludedQuestId.map(id -> !id.equals(quest.getId())).orElse(true))
                .filter(quest -> !activityRepository.findByQuestsIdOrderByOrderAsc(quest.getId()).isEmpty())
                .max(
                        Comparator
                                .comparing(
                                        Quest::getAssignedDate,
                                        Comparator.nullsFirst(Comparator.naturalOrder())
                                )
                                .thenComparing(Quest::getId)
                );
    }

    private void expire(List<pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser> questUsers) {
        for (var questUser : questUsers) {
            questUser.expire();
            questUserRepository.save(questUser);
        }
    }
}
