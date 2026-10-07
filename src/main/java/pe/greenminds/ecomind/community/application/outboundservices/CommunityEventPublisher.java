package pe.greenminds.ecomind.community.application.outboundservices;

import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalCompletedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;

/** Durable delivery port to implement with transactional outbox. No publisher implementation is registered yet. */
public interface CommunityEventPublisher {
  void publish(CommunityGoalCompletedIntegrationEvent event);
  void publish(PublicationCreatedIntegrationEvent event);
}
