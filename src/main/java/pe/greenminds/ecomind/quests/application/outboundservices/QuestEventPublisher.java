package pe.greenminds.ecomind.quests.application.outboundservices;

import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.FamilyPlanCompletedIntegrationEvent;

/** Publish from the validated completion transaction. Other completion types are contracts only for now. */
public interface QuestEventPublisher {
  void publish(QuestCompletedIntegrationEvent event);
  void publish(FamilyPlanCompletedIntegrationEvent event);
}
