package pe.greenminds.ecomind.community.application.internal.eventhandlers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.community.domain.model.events.CommunityGoalCompletedEvent;
import pe.greenminds.ecomind.community.domain.model.events.EventCreatedEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventPublishedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalReachedIntegrationEvent;

/**
 * Turns two domain events of Community into public notices, so other bounded contexts do not
 * depend on the Community domain model. It only publishes; it does not change any rule.
 */
@Component
public class CommunityNoticeEventHandler {

    private final ApplicationEventPublisher publisher;

    public CommunityNoticeEventHandler(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    public void on(EventCreatedEvent event) {
        publisher.publishEvent(
                new CommunityEventPublishedIntegrationEvent(
                        fixedId("community-event-created:" + event.eventId()),
                        event.eventId(),
                        event.communityId(),
                        event.authorId(),
                        event.name(),
                        CommunityEventPublishedIntegrationEvent.Action.CREATED,
                        Instant.now()));
    }

    @EventListener
    public void on(CommunityGoalCompletedEvent event) {
        publisher.publishEvent(
                new CommunityGoalReachedIntegrationEvent(
                        fixedId("community-goal-reached:" + event.communityGoalId()),
                        event.communityGoalId(),
                        event.communityId(),
                        event.title(),
                        Instant.now()));
    }

    // The same source always gets the same id, so a consumer can ignore a repeated delivery
    private static UUID fixedId(String source) {
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }
}
