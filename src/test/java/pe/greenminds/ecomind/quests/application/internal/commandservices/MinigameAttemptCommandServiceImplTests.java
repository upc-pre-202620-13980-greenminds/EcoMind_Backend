package pe.greenminds.ecomind.quests.application.internal.commandservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.aggregates.MinigameAttempt;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.commands.FinishMinigameAttemptCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameAttemptRepository;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinigameAttemptCommandServiceImplTests {
    @Mock private MinigameAttemptRepository attemptRepository;
    @Mock private MinigameRepository minigameRepository;
    @Mock private QuestRepository questRepository;

    private MinigameAttemptCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MinigameAttemptCommandServiceImpl(
                attemptRepository, minigameRepository, questRepository);
    }

    @Test
    void scoreAtMinimumIsSuccessful() {
        var attempt = prepareAttemptWithMinimumScore(70);

        var result = service.handle(new FinishMinigameAttemptCommand(5L, 70, Map.of()));

        assertTrue(result.toOptional().orElseThrow().getSuccessful());
        assertSame(attempt, result.toOptional().orElseThrow());
    }

    @Test
    void scoreBelowMinimumIsNotSuccessful() {
        prepareAttemptWithMinimumScore(70);

        var result = service.handle(new FinishMinigameAttemptCommand(5L, 69, Map.of()));

        assertFalse(result.toOptional().orElseThrow().getSuccessful());
    }

    private MinigameAttempt prepareAttemptWithMinimumScore(int minimumScore) {
        var attempt = new MinigameAttempt(9L, 12L, 4L);
        attempt.setId(5L);
        var quest = new Quest(
                12L, 4L, "Minigame quest", Category.ENERGY, "Play",
                QuestType.MINIGAME, 10, new Reward(5, 20), 5, null,
                Theme.MINIGAME, null, 12L, 1, QuestPublicationStatus.PUBLISHED);
        var minigame = new Minigame(
                4L, "Eco game", null, "https://example.com/game",
                Map.of("minScore", minimumScore));

        when(attemptRepository.findById(5L)).thenReturn(Optional.of(attempt));
        when(questRepository.findById(12L)).thenReturn(Optional.of(quest));
        when(minigameRepository.findById(4L)).thenReturn(Optional.of(minigame));
        when(attemptRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        return attempt;
    }
}
