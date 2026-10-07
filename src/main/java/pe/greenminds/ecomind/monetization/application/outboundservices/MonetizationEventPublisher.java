package pe.greenminds.ecomind.monetization.application.outboundservices;

import pe.greenminds.ecomind.monetization.interfaces.acl.events.StreakProtectedIntegrationEvent;
import pe.greenminds.ecomind.monetization.interfaces.acl.events.StreakProtectionUnavailableIntegrationEvent;

/** Durable delivery port to implement with transactional outbox. No publisher implementation is registered yet. */
public interface MonetizationEventPublisher {
  void publish(StreakProtectedIntegrationEvent event);
  void publish(StreakProtectionUnavailableIntegrationEvent event);
}
