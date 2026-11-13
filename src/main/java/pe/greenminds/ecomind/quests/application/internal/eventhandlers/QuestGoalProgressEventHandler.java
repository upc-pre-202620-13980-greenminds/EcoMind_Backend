package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import java.util.List;
import java.util.Locale;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.domain.model.events.CollaborativeQuestCompletedEvent;
import pe.greenminds.ecomind.quests.domain.model.events.QuestCompletedEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestFinishedForGoalsIntegrationEvent;

@Service
public class QuestGoalProgressEventHandler {

    private final QuestRepository quests;
    private final CollabQuestMemberRepository members;
    private final QuestEventPublisher publisher;

    public QuestGoalProgressEventHandler(
            QuestRepository quests,
            CollabQuestMemberRepository members,
            QuestEventPublisher publisher) {
        this.quests = quests;
        this.members = members;
        this.publisher = publisher;
    }

    @EventListener
    public void on(QuestCompletedEvent event) {
        if (event.collaborativeSessionId() != null) {
            return;
        }

        var quest = quests.findById(event.questId())
                .orElseThrow(() -> new IllegalStateException("Completed quest not found"));

        publisher.publish(new QuestFinishedForGoalsIntegrationEvent(
                "quest-user:" + event.questUserId(),
                List.of(event.userId()),
                quest.getCategory().name().toLowerCase(Locale.ROOT),
                event.completedAt().toInstant()));
    }

    @EventListener
    public void on(CollaborativeQuestCompletedEvent event) {
        var quest = quests.findById(event.questId())
                .orElseThrow(() -> new IllegalStateException("Completed quest not found"));

        var userIds = members.findBySessionIdAndStatusIn(
                        event.sessionId(), List.of(CollabMemberStatus.ACCEPTED))
                .stream()
                .map(member -> member.getUserId())
                .distinct()
                .sorted()
                .toList();

        publisher.publish(new QuestFinishedForGoalsIntegrationEvent(
                "collaborative-session:" + event.sessionId(),
                userIds,
                quest.getCategory().name().toLowerCase(Locale.ROOT),
                event.completedAt().toInstant()));
    }
}