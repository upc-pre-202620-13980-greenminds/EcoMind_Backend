package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementAward;

public record AchievementAwardResource(UUID id, UUID achievementId, String scope,
    Long beneficiaryId, UUID sourceEventId, Instant awardedAt) {
  public static AchievementAwardResource from(AchievementAward award) {
    return new AchievementAwardResource(award.id(), award.achievementId(), award.scope().name(),
        award.beneficiaryId(), award.sourceEventId(), award.awardedAt());
  }
}
