package pe.greenminds.ecomind.gamification.interfaces.acl;

import org.springframework.stereotype.Service;

import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.time.LocalDate;

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

    public GamificationContextFacade(GamificationQueryService queries) {
        this.queries = queries;
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
}
