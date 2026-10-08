package pe.greenminds.ecomind.community.application.outboundservices;

import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalCompletedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;

/**
 * Reserved durable port for future Community producers. AchievementPublicationService currently
 * confirms posts with atomic synchronous Spring events.
 */
public interface CommunityEventPublisher {
    void publish(CommunityGoalCompletedIntegrationEvent event);

    void publish(PublicationCreatedIntegrationEvent event);
}
