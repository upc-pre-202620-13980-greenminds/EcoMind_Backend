package pe.greenminds.ecomind.learning.application;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.TutorialProgressEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.TutorialProgressRepository;
import pe.greenminds.ecomind.learning.interfaces.rest.resources.TutorialProgressResource;

@Service
public class TutorialService {
  private static final int TOTAL_STEPS = 7;
  private final TutorialProgressRepository repository;
  public TutorialService(TutorialProgressRepository repository) { this.repository = repository; }

  @Transactional
  public TutorialProgressResource progress(Long userId) {
    return TutorialProgressResource.from(repository.findByUserId(userId).orElseGet(() -> {
      var progress = new TutorialProgressEntity(); progress.setUserId(userId);
      progress.setCurrentStep(1); progress.setTotalSteps(TOTAL_STEPS); return repository.save(progress);
    }));
  }

  @Transactional
  public TutorialProgressResource completeStep(Long userId) {
    var progress = entity(userId);
    if (progress.isCompleted()) return TutorialProgressResource.from(progress);
    if (progress.getCurrentStep() >= TOTAL_STEPS) {
      throw new IllegalArgumentException("Complete the tutorial from its final step");
    }
    progress.setCurrentStep(progress.getCurrentStep() + 1);
    return TutorialProgressResource.from(repository.save(progress));
  }

  @Transactional
  public TutorialProgressResource complete(Long userId) {
    var progress = entity(userId);
    if (progress.getCurrentStep() != TOTAL_STEPS) {
      throw new IllegalArgumentException("There are pending tutorial steps");
    }
    progress.setCompleted(true); progress.setCompletedAt(Instant.now());
    return TutorialProgressResource.from(repository.save(progress));
  }

  private TutorialProgressEntity entity(Long userId) {
    progress(userId);
    return repository.findByUserId(userId).orElseThrow();
  }
}
