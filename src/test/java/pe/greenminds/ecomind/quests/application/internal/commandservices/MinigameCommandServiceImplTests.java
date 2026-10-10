package pe.greenminds.ecomind.quests.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.commands.*;
import pe.greenminds.ecomind.quests.domain.repositories.*;

@ExtendWith(MockitoExtension.class)
class MinigameCommandServiceImplTests {
  @Mock MinigameRepository games;
  @Mock MinigameAttemptRepository attempts;
  @Mock QuestRepository quests;
  @InjectMocks MinigameCommandServiceImpl service;

  @Test
  void scoreRuleIsRequired() {
    assertTrue(service.handle(new CreateMinigameCommand("Game", null, "/game", null)).isFailure());
    verifyNoInteractions(games);
  }

  @Test
  void scoreRuleMustBeNumeric() {
    assertTrue(
        service
            .handle(new CreateMinigameCommand("Game", null, "/game", Map.of("minScore", "70")))
            .isFailure());
    verifyNoInteractions(games);
  }

  @Test
  void validGamePersistsCompletionRule() {
    when(games.save(any())).thenAnswer(i -> i.getArgument(0));
    var game =
        service
            .handle(new CreateMinigameCommand("Game", null, "/game", Map.of("minScore", 70)))
            .toOptional()
            .orElseThrow();
    assertEquals(70, game.getCompletionRules().get("minScore"));
  }

  @Test
  void missingGameCannotBeDeleted() {
    assertTrue(service.handle(new DeleteMinigameCommand(3L)).isFailure());
    verifyNoInteractions(attempts);
  }

  @Test
  void gameReferencedByQuestCannotBeDeleted() {
    when(games.findById(3L))
        .thenReturn(Optional.of(new Minigame(3L, "Game", null, "/game", Map.of("minScore", 70))));
    when(quests.existsByMinigameId(3L)).thenReturn(true);
    assertTrue(service.handle(new DeleteMinigameCommand(3L)).isFailure());
    verifyNoInteractions(attempts);
  }

  @Test
  void deletingUnusedGameDeletesAttemptsFirst() {
    when(games.findById(3L))
        .thenReturn(Optional.of(new Minigame(3L, "Game", null, "/game", Map.of("minScore", 70))));
    assertTrue(service.handle(new DeleteMinigameCommand(3L)).isSuccess());
    var ordered = inOrder(attempts, games);
    ordered.verify(attempts).deleteByMinigameId(3L);
    ordered.verify(games).deleteById(3L);
  }
}
