package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.stereotype.Service;

import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.RewardQueryService;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetRewardTransactionsQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.BeneficiaryType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardBeneficiary;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/** Public, read-only snapshot for other bounded contexts. Does not decide protector eligibility. */
@Service
public class GamificationContextFacade {
    public record Progress(
            Long userId,
            long ecopoints,
            int currentStreak,
            int longestStreak,
            LocalDate lastActivityDate) {}

    private final GamificationQueryService queries;
    // Used only by the read-only methods that Notifications needs
    private final AchievementQueryService achievementQueries;
    private final RewardQueryService rewardQueries;

    public GamificationContextFacade(
            GamificationQueryService queries,
            AchievementQueryService achievementQueries,
            RewardQueryService rewardQueries) {
        this.queries = queries;
        this.achievementQueries = achievementQueries;
        this.rewardQueries = rewardQueries;
    }

    public Progress getUserProgress(Long userId) {
        var progress = queries.getUserProgress(new UserId(userId));
        return new Progress(
                userId,
                progress.getTotalEcopoints(),
                progress.getCurrentStreak(),
                progress.getLongestStreak(),
                progress.getLastActivityDate());
    }

    /** Name of an achievement of the catalog; empty when it does not exist. */
    public Optional<String> findAchievementName(UUID achievementId) {
        return achievementQueries.find(achievementId).toOptional().map(a -> a.name());
    }

    /** EcoPoints the user earned in the period, read from the own reward history of the user. */
    public long getEcopointsEarned(Long userId, Instant from, Instant to) {
        var beneficiary = new RewardBeneficiary(BeneficiaryType.USER, userId);
        var period = new RankingPeriod(from, to);
        long total = 0;
        int page = 0;
        boolean hasNext = true;
        while (hasNext) {
            var result =
                    rewardQueries.handle(
                            new GetRewardTransactionsQuery(
                                    beneficiary, new UserId(userId), period, page, 100));
            var rewards = result.toOptional();
            if (rewards.isEmpty()) return total;
            for (var entry : rewards.get().items()) total += entry.grantedReward().ecopoints();
            hasNext = rewards.get().hasNext();
            page++;
        }
        return total;
    }
}
