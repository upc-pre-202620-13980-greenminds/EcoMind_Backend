package pe.greenminds.ecomind.learning.interfaces.rest.resources;

import java.time.Instant;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.TutorialProgressEntity;

public record TutorialProgressResource(Long id, int currentStep, int totalSteps,
    boolean completed, Instant completedAt) {
  public static TutorialProgressResource from(TutorialProgressEntity entity) {
    return new TutorialProgressResource(entity.getId(), entity.getCurrentStep(), entity.getTotalSteps(),
        entity.isCompleted(), entity.getCompletedAt());
  }
}
