package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;

import java.util.List;
import java.util.Optional;

public interface MinigameRepository {
    List<Minigame> findAll();
    Optional<Minigame> findById(Long id);
    boolean existsById(Long id);
    Minigame save(Minigame minigame);
    void deleteById(Long id);
}
