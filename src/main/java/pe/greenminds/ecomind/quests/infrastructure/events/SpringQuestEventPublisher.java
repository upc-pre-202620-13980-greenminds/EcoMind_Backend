package pe.greenminds.ecomind.quests.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.quests.application.outboundservices.QuestEventPublisher;
import pe.greenminds.ecomind.quests.interfaces.acl.events.QuestCompletedIntegrationEvent;
import pe.greenminds.ecomind.quests.interfaces.acl.events.FamilyPlanCompletedIntegrationEvent;

/** Publisher for the proposed reward-complete ACL contract; distinct from Quests' current events. */
@Component("proposedQuestEventPublisher")
@Transactional(propagation = Propagation.MANDATORY)
public class SpringQuestEventPublisher implements QuestEventPublisher {
  private final ApplicationEventPublisher publisher;
  public SpringQuestEventPublisher(ApplicationEventPublisher publisher) { this.publisher = publisher; }
  public void publish(QuestCompletedIntegrationEvent event) { publisher.publishEvent(event); }
  public void publish(FamilyPlanCompletedIntegrationEvent event) { publisher.publishEvent(event); }
}
