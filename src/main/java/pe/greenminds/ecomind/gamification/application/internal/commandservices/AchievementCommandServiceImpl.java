package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementAwardRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyScoreRepository;

@Service
public class AchievementCommandServiceImpl implements AchievementCommandService {
  private final AchievementRepository catalog;
  private final AchievementAwardRepository awards;
  private final UserProgressRepository users;
  private final FamilyScoreRepository families;

  public AchievementCommandServiceImpl(AchievementRepository catalog, AchievementAwardRepository awards,
      UserProgressRepository users, FamilyScoreRepository families) {
    this.catalog = catalog;
    this.awards = awards;
    this.users = users;
    this.families = families;
  }

  @Transactional
  public void register(Achievement achievement) { catalog.add(achievement); }

  @Transactional(propagation = Propagation.MANDATORY)
  public void evaluateUser(UserId userId, UUID sourceEventId, Instant occurredAt) {
    var progress = users.lockForReward(userId);
    for (var achievement : catalog.findActive(AchievementScope.INDIVIDUAL)) {
      long value = switch (achievement.metric()) {
        case ECOPOINTS -> progress.getTotalEcopoints();
        case EXPERIENCE -> progress.getTotalExperience();
        case LONGEST_STREAK -> progress.getLongestStreak();
      };
      awardIfSatisfied(achievement, userId.value(), value, sourceEventId, occurredAt);
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void evaluateFamily(FamilyId familyId, UUID sourceEventId, Instant occurredAt) {
    var score = families.lockForReward(familyId);
    for (var achievement : catalog.findActive(AchievementScope.FAMILY)) {
      awardIfSatisfied(achievement, familyId.value(), score.getTotalEcopoints(), sourceEventId, occurredAt);
    }
  }

  private void awardIfSatisfied(Achievement achievement, Long beneficiary, long value,
      UUID sourceEventId, Instant occurredAt) {
    if (achievement.isSatisfiedBy(value)) {
      awards.addIfAbsent(new AchievementAward(UUID.randomUUID(), achievement.id(), achievement.scope(),
          beneficiary, sourceEventId, occurredAt));
    }
  }
}
