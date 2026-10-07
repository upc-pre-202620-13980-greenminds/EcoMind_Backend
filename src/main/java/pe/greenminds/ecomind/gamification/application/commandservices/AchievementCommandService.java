package pe.greenminds.ecomind.gamification.application.commandservices;

import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

/** Trusted application port. Catalog administration and grants are not public mobile operations. */
public interface AchievementCommandService {
  void register(Achievement achievement);
  void evaluateUser(UserId userId, UUID sourceEventId, Instant occurredAt);
  void evaluateFamily(FamilyId familyId, UUID sourceEventId, Instant occurredAt);
}
