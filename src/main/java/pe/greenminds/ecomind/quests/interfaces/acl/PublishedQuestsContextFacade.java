package pe.greenminds.ecomind.quests.interfaces.acl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.MinigameAttemptStatus;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;

import java.time.Instant;
import java.util.List;

/** Read contract of the published Quests model. Exposes no persistence entities. */
@Service
@Transactional(readOnly = true)
public class PublishedQuestsContextFacade {
    public record ValidatedAttempt(
            Long attemptId,
            Long questId,
            Long minigameId,
            Long userId,
            Integer score,
            Instant completedAt) {}

    private final MinigameAttemptRepository attempts;

    public PublishedQuestsContextFacade(MinigameAttemptRepository attempts) {
        this.attempts = attempts;
    }

    public List<ValidatedAttempt> findValidatedAttempts(
            Long user, Long minigame, Instant from, Instant to) {
        if (user == null
                || user <= 0
                || minigame == null
                || minigame <= 0
                || from == null
                || to == null
                || from.isAfter(to))
            throw new IllegalArgumentException("Invalid attempt history query");
        return attempts.findByUserIdAndMinigameId(user, minigame).stream()
                .filter(
                        a ->
                                a.getStatus() == MinigameAttemptStatus.COMPLETED
                                        && Boolean.TRUE.equals(a.getSuccessful())
                                        && a.getEndDate() != null)
                .filter(
                        a ->
                                !a.getEndDate().toInstant().isBefore(from)
                                        && !a.getEndDate().toInstant().isAfter(to))
                .map(
                        a ->
                                new ValidatedAttempt(
                                        a.getId(),
                                        a.getQuestId(),
                                        a.getMinigameId(),
                                        a.getUserId(),
                                        a.getScore(),
                                        a.getEndDate().toInstant()))
                .toList();
    }
}
