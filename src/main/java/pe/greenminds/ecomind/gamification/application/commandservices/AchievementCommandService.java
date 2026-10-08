package pe.greenminds.ecomind.gamification.application.commandservices;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.AchievementShareRequest;
import pe.greenminds.ecomind.gamification.domain.model.commands.AwardAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ConfirmAchievementPublicationCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.ShareAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Trusted application port. Catalog administration and grants are not public mobile operations. */
public interface AchievementCommandService {
    void handle(AwardAchievementCommand command);

    void register(Achievement achievement);

    void evaluateCommunity(
            Long communityId, List<Long> eligible, UUID executionId, Instant occurredAt);

    void recognizeFamilyPlan(FamilyId familyId, UUID executionId, Instant occurredAt);

    Result<AchievementShareRequest, ApplicationError> handle(ShareAchievementCommand command);

    void handle(ConfirmAchievementPublicationCommand command);

    void evaluateUser(UserId userId, UUID sourceEventId, Instant occurredAt);

    void evaluateFamily(FamilyId familyId, UUID sourceEventId, Instant occurredAt);
}
