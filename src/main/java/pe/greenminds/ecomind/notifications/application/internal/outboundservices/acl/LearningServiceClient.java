package pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.learning.interfaces.acl.LearningContextFacade;
import pe.greenminds.ecomind.learning.interfaces.acl.LearningContextFacade.PendingMaterial;

/** Reads the pending materials of Learning for the learning reminders (HU-025). */
@Service("notificationsLearningServiceClient")
public class LearningServiceClient {

  private final LearningContextFacade learningContextFacade;

  public LearningServiceClient(LearningContextFacade learningContextFacade) {
    this.learningContextFacade = learningContextFacade;
  }

  public List<PendingMaterial> findPendingMaterials(Instant savedAfter, Instant savedBefore) {
    return learningContextFacade.findPendingMaterials(savedAfter, savedBefore);
  }
}
