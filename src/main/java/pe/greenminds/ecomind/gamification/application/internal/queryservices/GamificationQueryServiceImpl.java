package pe.greenminds.ecomind.gamification.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.UserProgress;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardTransactionRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GamificationQueryServiceImpl implements GamificationQueryService {
    private final UserProgressRepository progressRepository;
    private final RewardTransactionRepository rewardRepository;

    public GamificationQueryServiceImpl(
            UserProgressRepository progressRepository,
            RewardTransactionRepository rewardRepository) {
        this.progressRepository = progressRepository;
        this.rewardRepository = rewardRepository;
    }

    @Override
    public UserProgress getUserProgress(UserId userId) {
        return progressRepository.findByUserId(userId).orElseGet(() -> UserProgress.empty(userId));
    }

    @Override
    public List<RewardTransaction> getRecentRewards(UserId userId) {
        return rewardRepository.findRecentByUser(userId);
    }
}
